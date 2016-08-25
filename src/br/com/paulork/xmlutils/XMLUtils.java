package br.com.paulork.xmlutils;

import br.com.paulork.exceptions.XMLException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Paulo R. Kraemer <paulork10@gmail.com>
 */
public class XMLUtils {

    // Árvore DOM
    private Document doc;
    // XML no disco
    private File file;
    // Reflete as alteração diretamente para o disco. False por default. Assim 
    // realize as alterações e posteriormente usar o metodo "save" para salvar 
    // todas as alterações de uma só vez.
    private boolean autoFlush = false;
    // Faz a leitura do arquivo em disco a cada solicitação de leitura. False
    // por default.
    private boolean autoLoad = false;
    private String charset = "UTF-8";

    public static final String UTF_8 = "UTF-8";
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * Construtor recebe um File do arquivo a ser lido.
     *
     * @param file File do arquivo a ser lido.
     */
    public XMLUtils(File file) {
        if (file != null && file.exists()) {
            this.file = file;
            readXML();
        } else {
            throw new XMLException("O arquivo \"" + file.getAbsolutePath() + "\" não existe.");
        }
    }

    /**
     * Construtor recebe uma string com o caminho do arquivo a ser lido.
     *
     * @param file String com o caminho do arquivo a ser lido
     */
    public XMLUtils(String file) {
        this(new File(file));
    }

    /**
     * Construtor recebe um StringBuilder com o conteúdo do arquivo xml em sí.
     * Esse conteúdo, após ser manipulado, pode ser salvo em disco utilizando-se
     * o método "save(file)".
     *
     * @param xml
     * @throws Exception
     */
    public XMLUtils(StringBuilder xml) throws Exception {
        this.doc = strToDoc(xml);
    }

    /**
     * Contrutor que recebe um Document externo com o conteúdo xml. Esse
     * procedimento não vincula o Document (árvore DOM) a um arquivo fisico no
     * disco, portanto o método \"save()\" não irá funcionar. Para gravar o
     * xml para o disco é necessário usar o metodo \"save(file)\". Vale
     * lembrar que os recursos de auto-leitura e auto-gravação também não
     * funcionarão.
     *
     * @param document
     */
    public XMLUtils(Document document) {
        this.doc = document;
        this.file = null;
    }

    private void readXML() {
        String str = "";
        StringBuilder sb;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        if (file != null) {
            if (file.exists()) {
                try {
                    sb = new StringBuilder();
                    fis = new FileInputStream(file);
                    isr = new InputStreamReader(fis, Charset.forName(this.charset));
                    reader = new BufferedReader(isr);
                    while ((str = reader.readLine()) != null) {
                        sb.append(str);
                    }
                    str = null;
                    fis.close();
                    reader.close();
                    fis = null;
                    isr.close();
                    isr = null;
                    reader = null;

                    //faz o parse do arquivo e cria o Document
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db;
                    db = dbf.newDocumentBuilder();
                    doc = db.parse(new InputSource(new StringReader(sb.toString())));
                } catch (SAXException | ParserConfigurationException ex) {
                    ex.printStackTrace();
                    throw new XMLException("Erro ao fazer o parse do arquivo XML.", ex);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new XMLException("Erro de leitura do arquivo XML.", ex);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    throw new XMLException("Parametro informado é nulo.", ex);
                }
            } else {
                throw new XMLException("Arquivo informado não existe: [" + file.getAbsolutePath() + "]");
            }
        } else {
            System.err.println("Auto Load ativado, mas nenhum arquivo foi informado. Possivelmente o xml foi carregado a partir de uma string.");
        }
    }

    /**
     * Retorna o valor que está entre a tag informada. Retorna apenas o valor da
     * primeira ocorrência da tag, para retornar os valores de todas as
     * ocorrencias da tag use <code>getValues(tag)</code>.
     *
     * @param tag Nome da tag.
     * @return Retorna o valor da tag.
     * @exception Exception
     * @see #getValues(String)
     */
    public String getValue(String tag) {
        if (autoLoad) {
            readXML();
        }
        
        NodeList nodeList = doc.getElementsByTagName(tag);
        if(nodeList.getLength() > 0){
            return nodeList.item(0).getTextContent();
        } else {
            return null;
        }
    }

    /**
     * Retorna todos os valores de todas as ocorrências da tag em ordem
     * crescente da primeira encontrada até a ultima.
     *
     * @param tag Nome da tag.
     * @return Retorna um array com os valores de todas as ocorrências da tag.
     * @exception Exception
     * @see #getValue(String)
     */
    public String[] getValues(String tag) throws Exception {
        if (autoLoad) {
            readXML();
        }
        try {
            int numTags = doc.getElementsByTagName(tag).getLength();
            String[] values = new String[numTags];
            for (int i = 0; i < numTags; i++) {
                values[i] = doc.getElementsByTagName(tag).item(i).getTextContent();
            }
            return values;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna os nomes dos atributos da tag informada. Retorna apenas os nomes
     * dos atributos da primeira ocorrência da tag, as outras ocorrências serão
     * ignoradas.
     *
     * @param tag Nome da tag.
     * @return Retorna um array com os nomes dos atributos da tag.
     * @exception Exception
     * @see #getAttributeValues(String)
     */
    public String[] getAttributes(String tag) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            int numAttr = doc.getElementsByTagName(tag).item(0).getAttributes().getLength();
            String[] attributes = new String[numAttr];
            for (int i = 0; i < numAttr; i++) {
                attributes[i] = doc.getElementsByTagName(tag).item(0).getAttributes().item(i).getNodeName();
            }
            return attributes;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna os nomes dos atributos da tag informada. Nessa opção você
     * consegue retornar todas as ocorrências da TAG. Para isso basta se
     * utilizar do método <code>getNumOccur(tag)</code> que retorna o
     * número de ocorrência da tag no XML e se utilizar desse indice. <b>EX:</b><pre>
     * {@code
     * for(int i = 0; i < arq.getNumOccur(tag); i++){
     *   arq.getAttributes(tag, i);
     * }
     * }</pre>
     *
     * @param tag Nome da tag.
     * @param index Indice da tag.
     * @return Retorna um array com os nomes dos atributos da tag.
     * @exception Exception
     * @see #getAttributeValues(String)
     * @see #getAttributes(String)
     */
    public String[] getAttributes(String tag, int index) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            int numAttr = doc.getElementsByTagName(tag).item(index).getAttributes().getLength();
            String[] attributes = new String[numAttr];
            for (int i = 0; i < numAttr; i++) {
                attributes[i] = doc.getElementsByTagName(tag).item(index).getAttributes().item(i).getNodeName();
            }
            return attributes;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna os valores dos atributos da tag informada. Retorna apenas os
     * valores dos atributos da primeira ocorrência da tag, as outras
     * ocorrências serão ignoradas.
     *
     * @param tag Nome da tag.
     * @return Retorna um array com os valores dos atributos da tag.
     * @exception Exception
     * @see #getAttributes(String)
     */
    public String[] getAttributeValues(String tag) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            int numAttr = doc.getElementsByTagName(tag).item(0).getAttributes().getLength();
            String[] values = new String[numAttr];
            for (int i = 0; i < numAttr; i++) {
                values[i] = doc.getElementsByTagName(tag).item(0).getAttributes().item(i).getNodeValue();
            }
            return values;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna os valores dos atributos da tag informada. Nessa opção você
     * consegue retornar todas as ocorrências da TAG. Para isso basta se
     * utilizar do método <code>getNumOccur(tag)</code> que retorna o
     * número de ocorrência da tag no XML e se utilizar desse indice.
     *
     * @param tag Nome da tag.
     * @param index Indice da tag.
     * @return Retorna um array com os valores dos atributos da tag.
     * @exception Exception
     * @see #getAttributes(tag)
     * @see #getAttributeValues(tag)
     */
    public String[] getAttributeValues(String tag, int index) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            int numAttr = doc.getElementsByTagName(tag).item(index).getAttributes().getLength();
            String[] values = new String[numAttr];
            for (int i = 0; i < numAttr; i++) {
                values[i] = doc.getElementsByTagName(tag).item(index).getAttributes().item(i).getNodeValue();
            }
            return values;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna um HashMap no esquema [CHAVE, VALOR], onde a CHAVE seria o nome
     * das tags filhas e o VALOR, o valor das respectivas tags filhas.<br/><br/>
     * <b>EX:</b>
     * <pre>
     * {@code
     * <funcionario>
     *     <nome>João da Silva</nome>
     *     <funcao>Serviços Gerais</funcao>
     * </funcionario>
     * }
     * </pre> se você usar <code><b>getMapChilds("funcionario")</b></code> vai
     * ser retornado um {@code HashMap<chave, valor>} onde <code>nome</code> e
     * <code>funcao</code> são as chaves e <code>João da Silva</code> e
     * <code>Serviços Gerais</code> são os os valores das respectivas
     * chaves.<br/><br/> 
     * <b>USO:</b><br/>
     * <pre>{@code
     * HashMap<String, String> map = getMapChilds("funcionario");
     * map.get("nome"); //Retorna "João da Silva"
     * map.get("funcao"); //Retorna "Serviços Gerais"
     * }</pre>
     *
     * @param tag Nome da tag.
     * @return Retorna um {@code HashMap<String, String>} com os nomes e os
     * valores das tags filhas da tag informada.
     * @exception Exception
     */
    public HashMap<String, String> getMapChilds(String tag) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            int count = 0;
            NodeList nodeList = doc.getElementsByTagName(tag).item(0).getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (!"#text".equals(nodeList.item(i).getNodeName()) && !"#comment".equals(nodeList.item(i).getNodeName())) {
                    count++;
                }
            }
            HashMap<String, String> map = new HashMap<String, String>();
            int a = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (!"#text".equals(nodeList.item(i).getNodeName()) && !"#comment".equals(nodeList.item(i).getNodeName())) {
                    map.put(nodeList.item(i).getNodeName(), nodeList.item(i).getTextContent());
                    a++;
                }
            }
            return map;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna um HashMap no esquema [CHAVE, VALOR], onde a CHAVE seria o nome
     * dos atributos da TAG e o VALOR, o valor dos respectivos atributos para a
     * primeira instância da TAG.<br/><br/> <b>EX:</b>
     * <pre>
     * {@code
     *     <nome param1="valor1" param2="valor2" />
     *     <funcao param1="valor1" param2="valor2" />
     * }
     * </pre> se você usar <code><b>getMapAttributes("nome")</b></code> vai ser
     * retornado um {@code HashMap<chave, valor>} onde <code>param1</code> e
     * <code>param2</code> são as chaves e <code>valor1</code> e
     * <code>valor2</code> são os os valores dos respectivos
     * atributos.<br/><br/> <b>USO:</b><br/>
     * <pre>
     * {@code
     * HashMap<String, String> nome = getMapAttributes("nome");
     * map.get("param1"); //Retorna "valor1"
     * map.get("param2"); //Retorna "valor2"
     * }
     * </pre>
     *
     * @param tag Nome da tag.
     * @return Retorna um {@code HashMap<String, String>} com os nomes e os
     * valores dos atributos da tag informada.
     * @exception Exception
     */
    public HashMap<String, String> getMapAttributes(String tag) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            String[] names = XMLUtils.this.getAttributes(tag);
            String[] values = XMLUtils.this.getAttributeValues(tag);
            if (names.length == values.length) {
                for (int i = 0; i < names.length; i++) {
                    map.put(names[i], values[i]);
                }
            } else {
                throw new Exception("Lista de \"atributos\" difere da lista de \"valores de atributos\".");
            }
            return map;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Retorna um HashMap no esquema [CHAVE, VALOR], onde a CHAVE seria o nome
     * dos atributos da TAG e o VALOR, o valor dos respectivos atributos para a
     * todas instâncias da TAG, acessadas de acordo com seu "index" (posição que
     * se encontra dentro do XML).<br/><br/> <b>EX:</b>
     * <pre>
     * {@code
     *     <nome param1="valor1" param2="valor2" />
     *     <funcao param1="valor1" param2="valor2" />
     * }
     * </pre> se você usar <code><b>getMapAttributes("nome")</b></code> vai ser
     * retornado um {@code HashMap<chave, valor>} onde <code>param1</code> e
     * <code>param2</code> são as chaves e <code>valor1</code> e
     * <code>valor2</code> são os os valores dos respectivos
     * atributos.<br/><br/> 
     * <b>USO:</b><br/>
     * <pre>
     * {@code
     * HashMap<String, String> nome = getMapAttributes("nome");
     * map.get("param1"); //Retorna "valor1"
     * map.get("param2"); //Retorna "valor2"
     * }
     * </pre>
     *
     * @param tag Nome da tag.
     * @return Retorna um {@code HashMap<String, String>} com os nomes e os
     * valores dos atributos da tag informada.
     * @see getNumOccur(tag)
     * @exception Exception
     */
    public HashMap<String, String> getMapAttributes(String tag, int index) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            String[] names = getAttributes(tag, index);
            String[] values = getAttributeValues(tag, index);
            if (names.length == values.length) {
                for (int i = 0; i < names.length; i++) {
                    map.put(names[i], values[i]);
                }
            } else {
                throw new Exception("Lista de \"atributos\" difere da lista de \"valores de atributos\".");
            }
            return map;
        } catch (Exception ex) {
            throw new Exception("A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Use este método para setar o valor de alguma tag. Basta informar a tag a
     * qual deseja alterar e o seu novo valor. Será alterada apenas o valor da
     * primeira ocorrência da tag, se houver outras, serão ignoradas.
     *
     * @param tag Nome da tag.
     * @param value Valor da tag
     * @throws Exception
     * @see #setValues(tag, value, index)
     */
    public void setValue(String tag, String value) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                throw new Exception(ex);
            }
        }
        try {
            doc.getDocumentElement().getElementsByTagName(tag).item(0).setTextContent(value);
            if (autoFlush) {
                save();
            }
        } catch (Exception ex) {
            throw new Exception("Erro ao setar o valor da tag. A tag [" + tag + "] especificada não existe no XML.", ex);
        }
    }

    /**
     * Seta os valores de todas as ocorrências da tag informada. Para cada
     * ocorrência pode-se ter um valor diferente. Isso é garantido pelo
     * parametro "index" que especifica qual ocorrência será alterada. Os
     * valores das tags serão alterados conforme as tags forem sendo
     * encontradas, do inicio do arquivo até o final.<br/> <b>EX:</b> se o
     * "indice" for 2 a terceira ocorrência da tag será alterada, pois os
     * valores começam em 0 (zero). <br/><br/> Para saber quantas ocorrências
     * existem para alguma tag use <code>getNumOccur()</code>.
     *
     * @param tag Nome da tag.
     * @param value Valor da tag.
     * @param index Indice da tag.
     * @throws Exception
     * @see #setValue(tag, value)
     */
    public void setValues(String tag, String value, Integer index) throws Exception {
        try {
            NodeList nodeList = doc.getDocumentElement().getElementsByTagName(tag);
            Integer numItems = nodeList.getLength();
            if (index > numItems || index == null || index < 0) {
                throw new Exception("Indice informado ['" + index + "'] está fora da faixa ou nulo.");
            } else {
                doc.getDocumentElement().getElementsByTagName(tag).item(index).setTextContent(value);
                if (autoFlush) {
                    save();
                }
            }
        } catch (Exception ex) {
            throw new Exception("Erro ao setar o valor da tag. A tag [" + tag + "]  especificada não existe no XML.", ex);
        }
    }

    /**
     * Grava o arquivo que está sendo manipulado em memória (no caso de
     * AutoFlush ser false). O arquivo original será substituido.
     *
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see #save(file)
     */
    public void save() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (file != null) {
            save(file);
        }
    }

    /**
     * Grava o arquivo que está sendo manipulado em memória (no caso de
     * AutoFlush ser false). É necessário informar um novo nome para o arquivo.
     *
     * @param file Nome do novo arquivo.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see #save()
     */
    public void save(String file) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        save(new File(file));
    }

    /**
     * Grava o arquivo que está sendo manipulado em memória (no caso de
     * AutoFlush ser false). É necessário informar um novo nome para o arquivo.
     *
     * @param file File do arquivo a ser gravado.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see #save()
     */
    public void save(File file) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        try {
            String xml = docToStr(doc);
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), this.charset));
            writer.write(xml);
            writer.flush();
            writer.close();
            writer = null;
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Erro ao gravar \"" + file.getName() + "\". Arquivo não encontrado. [" + ex.getMessage() + "]");
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedEncodingException("Erro ao gravar \"" + file.getName() + "\". Codificação especificada não é valida. [" + ex.getMessage() + "]");
        } catch (IOException ex) {
            throw new IOException("Erro ao gravar \"" + file.getName() + "\". Erro de entrada/saida.", ex);
        }

        try {
            readXML();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retorna o numero de ocorrências da TAG.
     *
     * @param tag Nome da tag.
     * @return Retorna um inteiro com o numero de ocorrências da TAG no XML.
     * @throws Exception
     */
    public Integer getNumOccur(String tag) throws Exception {
        if (autoLoad) {
            try {
                readXML();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Integer numOcor;
        try {
            numOcor = doc.getDocumentElement().getElementsByTagName(tag).getLength();
        } catch (Exception ex) {
            throw new Exception("Erro ao resgatar a quantidade de ocorrências da tag ['" + tag + "'].", ex);
        }
        return numOcor;
    }

    /**
     * Transforma um StringBuilder (representando um XML) em um Document.
     *
     * @param xml StringBuilder representando o XML
     * @return Document do XML.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document strToDoc(StringBuilder xml) throws ParserConfigurationException, SAXException, IOException {
        return strToDoc(xml.toString());
    }

    /**
     * Transforma uma string XML para um Document.
     *
     * @param xml String do XML
     * @return Document do XML.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document strToDoc(String xml) throws ParserConfigurationException, SAXException, IOException {
        Document xDoc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xDoc = builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException ex) {
            throw new ParserConfigurationException("Mensagem: " + ex.getMessage());
        } catch (SAXException ex) {
            throw new SAXException("Erro ao fazer o parser do XML. Mensagem: " + ex.getMessage());
        } catch (IOException ex) {
            throw new IOException("Erro de I/O. Mensagem: " + ex.getMessage());
        }
        return xDoc;
    }

    /**
     * Adiciona uma seção CDATA (e seu conteúdo) a um tag especificada.
     *
     * @param tag Tag na qual será adicionada a seção CDATA
     * @param value Valor da seção CDATA (conteúdo)
     */
    public void setCDATA(String tag, String value) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        doc.getDocumentElement().getElementsByTagName(tag).item(0).setTextContent("");
        doc.getDocumentElement().getElementsByTagName(tag).item(0).appendChild(doc.createCDATASection(value));
        if (autoFlush) {
            XMLUtils.this.save();
        }
    }

    /**
     * Cria/Adiciona uma nova tag ao corpo principal (raíz) do XML.
     *
     * @param tag Tag a ser criada
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see createTag(parent, tag)
     * @see createTag(parent, tag, content)
     */
    public void createTag(String tag) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        createTag(null, tag, null);
    }

    /**
     * Cria/Adiciona uma tag ("tag") em uma outra tag ("parent").
     *
     * Multiplas adições da mesma tag, gerarão multiplas tags (filhas) de mesmo
     * nome.
     *
     * @param parent Tag pai (container)
     * @param tag Tag a ser criada
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see createTag(tag)
     * @see createTag(parent, tag, content)
     */
    public void createTag(String parent, String tag) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        createTag(parent, tag, null);
    }

    /**
     * Cria/Adiciona uma tag (tag filha (e seu conteúdo)) em uma outra tag (tag
     * pai).
     *
     * Multiplas adições a mesma TAG (com o mesmo nome), gerarão multiplas TAGs
     * filhas de mesmo nome, mas com conteúdos não necessariamente iguais.
     *
     * @param parent Tag pai (container).
     * @param tag Tag a ser criada.
     * @param content Conteúdo da tag a ser criada.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see createTag(tag)
     * @see createTag(parent, tag)
     */
    public void createTag(String parent, String tag, String content) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        Element el = doc.createElement(tag);
        if (content != null && !content.isEmpty()) {
            el.setTextContent(content);
        }
        if (parent != null && !parent.isEmpty()) {
            doc.getElementsByTagName(parent).item(0).appendChild(el);
        } else {
            doc.getDocumentElement().appendChild(el);
        }
        if (autoFlush) {
            XMLUtils.this.save();
        }
    }

    /**
     * Realiza a remoção de uma tag. Mas apenas a primeira ocorrência da tag.
     * Para remover multiplas ocorrências utilize "removeTag(tag, index)" em
     * conjunto com "getNumOccur(tag)" para pegar todas as ocorrências da mesma
     * tag.
     *
     * Mas cuidado com regravações sucessivas, pois pode acabar corrompendo o
     * arquivo.
     *
     * @param tag Nome da tag a ser removida
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see removeTag(tag, index)
     * @see getNumOccur(tag)
     */
    public void removeTag(String tag) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        Element e = (Element) doc.getElementsByTagName(tag).item(0);
        e.getParentNode().removeChild(e);
        if (autoFlush) {
            XMLUtils.this.save();
        }
    }

    /**
     * Habilita/Desabilita a auto-gravação do arquivo XML. Por padrão está
     * desativada para todos os métodos que fazem alteração na estrutura do XML
     * em memória, como por ex: setValue, setValues, removeTag, setCDATA,
     * createTag, etc.
     *
     * Tomar cuidado ao habilitar esta opção em procedimentos de gravações
     * sucessivas, pois pode ocorrer corrupção do arquivo XML. Nesses casos,
     * desabilite a funcionalidade, faça as alterações necessárias e no final
     * chame o método "save" para persistir as alterações.
     *
     * @param enable Ativa/Desativa a auto-gravação do XML.
     */
    public void setAutoFlush(boolean enable) {
        this.autoFlush = enable;
    }

    /**
     * Habilita/Desabilita a auto-leitura do arquivo XML. Por padrão está
     * desativada para todos os métodos que fazem leitura da estrutura do XML em
     * memória, como por ex: getValue, getValues, getMapChilds, getNumOccur,
     * etc.
     *
     * @param enable Ativa/Desativa a auto-leitura do XML.
     */
    public void setAutoLoad(boolean enable) {
        this.autoLoad = enable;
    }

    public File getFile() {
        return file;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) throws Exception {
        if (charset != null && !charset.isEmpty()) {
            this.charset = charset;
        } else {
            throw new Exception("O valor do charset não poder ser nulo ou em branco.\n"
                    + "Use [XMLUtils.UTF_8] ou [XMLUtils.ISO_8859_1] para especificar o charset.\n"
                    + "O charset padrão quando não especificado é UTF-8.");
        }
    }

    @Override
    public String toString() {
        return docToStr(doc);
    }

    /**
     * Converte um Document em uma representação em String (texto). Indentação
     * por padrão. Declaração omitida por padrão.
     *
     * @param document Document que será convertido para string
     * @return XML (em formato string)
     * @see docToStr(document, indent, omit_declaration)
     */
    public String docToStr(Document document) {
        return docToStr(document, true, true);
    }

    /**
     * Converte um Document em uma representação em String (texto).
     *
     * @param document Document que será convertido para string
     * @param indent Indica que se o XML será indentado
     * @param omit_declaration A declaração do XML será omitida
     * @return XML (em formato string)
     */
    public String docToStr(Document document, boolean indent, boolean omit_declaration) {
        try {
            // Garante a indentação --------------------------------------------
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
            // -----------------------------------------------------------------
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        }

        try {
            // Faz a transformação ---------------------------------------------
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, (indent ? "yes" : "no"));
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, (omit_declaration ? "yes" : "no"));
            tr.setOutputProperty(OutputKeys.ENCODING, this.charset);
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            Writer out = new StringWriter();
            tr.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
            // -----------------------------------------------------------------
        } catch (TransformerException te) {
            te.printStackTrace();
            return null;
        }
    }

    /**
     * Transforma parte de um XML, em um novo Document. Em outras palavras,
     * consiste em pegar uma sub-árvore de um XML e transformá-lo um novo XML.
     *
     * @param parent_tag Tag do xml (original) que será a tag raiz do novo XML
     * @return Document representando o novo XML criado a partir do fragmento
     */
    public Document getSubDocument(String parent_tag) {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(
                    new DOMSource(doc.getElementsByTagName(parent_tag).item(0)),
                    new StreamResult(sw)
            );
            return strToDoc(sw.toString());
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Document getDocument() {
        return doc;
    }

    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<teste>");
        sb.append("  <cod>007</cod>");
        sb.append("  <msg>Hello world</msg>");
        sb.append("</teste>");
        XMLUtils xml = new XMLUtils(sb);
        xml.setAutoLoad(true);
        System.out.println(xml.docToStr(xml.strToDoc(sb), true, true));
        System.out.println("------------------------------");
        System.out.println(xml.docToStr(xml.strToDoc(sb), false, false));
        System.out.println("------------------------------");
        System.out.println(xml.docToStr(xml.strToDoc(sb), false, true));
        System.out.println("------------------------------");
        System.out.println(xml.getValue("cod"));
        System.out.println("------------------------------");
        System.out.println("Nulo aqui: "+xml.getValue("msg1"));
        System.out.println("------------------------------");
        System.out.println("Sem erro.: "+xml.getValue("msg"));
    }

}
