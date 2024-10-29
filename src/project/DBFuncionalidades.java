package project;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.sql.PreparedStatement;

/**
 *
 * @author driicarvalho7
 */
public class DBFuncionalidades {
    Connection connection;
    Statement stmt;
    ResultSet rs;
    JTextArea jtAreaDeStatus;

    // String de conexão para o Schema ‘LAB_BD_PROJETO’ e com Autenticação do Windows
    String connectionUrl = "jdbc:sqlserver://ADRIANO_PC;databaseName=LAB_BD_PROJETO;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    /**
     * Construtor da classe DBFuncionalidades.
     *
     * @param jtaTextArea JTextArea para exibição de status e mensagens de erro.
     */
    public DBFuncionalidades(JTextArea jtaTextArea){
        jtAreaDeStatus = jtaTextArea;
    }

    /**
     * Recupera todos os dados de uma tabela do banco de dados.
     *
     * @param nomeTabela Nome da tabela a ser consultada.
     * @param numColunas Número de colunas na tabela.
     * @return Matriz bidimensional de Strings contendo os dados da tabela, ou
     *         uma matriz vazia em caso de erro.
     */
    private String[][] getDadosTabela(String nomeTabela, int numColunas) {
        String query = "SELECT * FROM " + nomeTabela;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ArrayList<String[]> linhasList = new ArrayList<>();
            while (rs.next()) {
                String[] linha = new String[numColunas];
                for (int i = 1; i <= numColunas; i++) {
                    linha[i - 1] = rs.getString(i) != null ? rs.getString(i) : "[null]";
                }
                linhasList.add(linha);
            }
            return linhasList.toArray(new String[0][0]);
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao recuperar dados da tabela: " + ex.getMessage());
        }
        return new String[0][0];
    }

    /**
     * Obtém as informações de metadados das colunas de uma tabela.
     *
     * @param nomeTabela Nome da tabela.
     * @return StringBuilder contendo metadados das colunas.
     */
    private StringBuilder getAllInformationColunas(String nomeTabela) {
        StringBuilder metaInfo = new StringBuilder();
        String query = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            try (ResultSet rs = stmt.executeQuery()) {
                metaInfo.append("Metadados da Tabela: ").append(nomeTabela).append("\n");
                while (rs.next()) {
                    metaInfo.append("Coluna: ").append(rs.getString("COLUMN_NAME"))
                            .append(" | Tipo: ").append(rs.getString("DATA_TYPE"))
                            .append(" | Tamanho: ").append(rs.getInt("CHARACTER_MAXIMUM_LENGTH"))
                            .append(" | Aceita Nulo: ").append("YES".equals(rs.getString("IS_NULLABLE")) ? "Sim" : "Não")
                            .append("\n");
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter colunas da tabela: " + ex.getMessage());
        }
        return metaInfo;
    }

    /**
     * Retorna os nomes das colunas de uma tabela em ordem.
     *
     * @param nomeTabela Nome da tabela.
     * @return Array de Strings com os nomes das colunas.
     */
    private String[] getNomeColunas(String nomeTabela) {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        ArrayList<String> colunasList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    colunasList.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter colunas da tabela: " + ex.getMessage());
        }
        return colunasList.toArray(String[]::new);
    }

    /**
     * Gera o DDL para criar uma tabela no banco de dados.
     *
     * @param tableName Nome da tabela.
     * @return String contendo o DDL da tabela.
     */
    private String getTableDDL(String tableName) {
        StringBuilder ddl = new StringBuilder();
        try {
            ddl.append("CREATE TABLE ").append(tableName).append(" (\n");
            String queryColumns = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
            try (PreparedStatement stmt = connection.prepareStatement(queryColumns)) {
                stmt.setString(1, tableName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        String dataType = rs.getString("DATA_TYPE");
                        int maxLength = rs.getInt("CHARACTER_MAXIMUM_LENGTH");
                        String isNullable = rs.getString("IS_NULLABLE");
                        ddl.append("    ").append(columnName).append(" ").append(dataType);
                        if ("varchar".equalsIgnoreCase(dataType)) {
                            ddl.append("(").append(maxLength).append(")");
                        }
                        ddl.append(" ").append("NO".equals(isNullable) ? "NOT NULL" : "NULL").append(",\n");
                    }
                }
            }
            ddl.setLength(ddl.length() - 2);
            ddl.append("\n);");
        } catch (SQLException e) {
            jtAreaDeStatus.setText("Erro ao gerar DDL da tabela " + tableName + ": " + e.getMessage());
        }
        return ddl.toString();
    }

    /**
     * Estabelece uma conexão com o banco de dados.
     *
     * @return true se a conexão for bem-sucedida, caso contrário, false.
     */
    public boolean conectar() {
        try {
            // Registro do driver JDBC do SQL Server
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

            // Estabelece a conexão
            connection = DriverManager.getConnection(connectionUrl);
            jtAreaDeStatus.setText("Conectado ao SQL Server com sucesso!");
            return true;
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao conectar ao SQL Server: " + ex.getMessage());
            System.out.println("Erro ao conectar ao SQL Server: " + ex.getMessage());
            Loadings.hideLoadingScreen();
        } finally {
            Loadings.hideLoadingScreen();
        }
        return false;
    }

    /**
     * Recupera o DDL de todas as tabelas do banco de dados.
     *
     * @return String contendo o DDL completo do schema.
     */
    public String getDDLTables() {
        StringBuilder ddlFinal = new StringBuilder();
        for (String table : getTablesName()) {
            ddlFinal.append(getTableDDL(table)).append("\n\n");
        }
        return ddlFinal.toString();
    }

    /**
     * Retorna os nomes de todas as tabelas do banco de dados.
     *
     * @return Array de Strings com os nomes das tabelas.
     */
    public String[] getTablesName() {
        ArrayList<String> table = new ArrayList<>();
        try {
            String s = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY table_name ASC";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                String t = rs.getString("table_name");
                table.add(t);
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter tabelas: " + ex.getMessage());
            Loadings.hideLoadingScreen();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                jtAreaDeStatus.setText("Erro ao fechar recursos: " + e.getMessage());
            } finally {
                Loadings.hideLoadingScreen();
            }
        }
        return table.toArray(String[]::new);
    }

    /**
     * Preenche um JComboBox com os nomes das tabelas do banco de dados.
     *
     * @param jc JComboBox a ser preenchido.
     */
    public void pegarNomesDeTabelas(JComboBox jc){
        String query = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY table_name ASC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                jc.addItem(rs.getString("table_name"));
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter nomes de tabelas: " + ex.getMessage());
        }
    }

    /**
     * Exibe os dados de uma tabela em uma JTable.
     *
     * @param jt JTable onde os dados serão exibidos.
     * @param nomeTabela Nome da tabela a ser exibida.
     */
    public void exibeDados(JTable jt, String nomeTabela){
        StringBuilder metaInfo = new StringBuilder();
        ResultSet rs = null; // Declare o ResultSet como local para este método
        Statement stmt = null;
        try {
            // Recebe todas as informações das colunas
            metaInfo = getAllInformationColunas(nomeTabela);

            // Recebe o nome das colunas
            String[] colunas = getNomeColunas(nomeTabela);

            // Consulta para obter todos os dados da tabela
            String query = "SELECT * FROM " + nomeTabela;
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);

            // Armazenar os dados em uma lista de arrays de string
            ArrayList<String[]> dadosList = new ArrayList<>();
            while (rs.next()) {
                String[] linha = new String[colunas.length];
                for (int i = 0; i < colunas.length; i++) {
                    linha[i] = rs.getString(colunas[i]);
                }
                dadosList.add(linha);
            }

            // Converter a lista para um array bidimensional
            String[][] dados = dadosList.toArray(new String[0][0]);

            // Atualizar a JTable com os dados e colunas
            DefaultTableModel model = (DefaultTableModel) jt.getModel();
            model.setDataVector(dados, colunas);

            // Atualizar o status com metadados
            jtAreaDeStatus.setText(metaInfo.toString());

        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao exibir dados da tabela: " + nomeTabela + ". Detalhes: " + ex.getMessage());
        } finally {
            // Fechar os recursos apenas se não forem nulos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                jtAreaDeStatus.setText("Erro ao fechar recursos: " + e.getMessage());
            } finally {
                Loadings.hideLoadingScreen();
            }
        }
    }

    /**
     * Insere dados na tabela especificada a partir de um painel de inserção.
     *
     * @param nomeTabela Nome da tabela onde os dados serão inseridos.
     * @param painelDeInsercao JPanel contendo os campos de entrada de dados.
     */
    public void inserirDados(String nomeTabela, JPanel painelDeInsercao) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + nomeTabela + " (");
        StringBuilder valores = new StringBuilder(" VALUES (");

        Component[] componentes = painelDeInsercao.getComponents();
        ArrayList<String> colunas = new ArrayList<>();
        ArrayList<String> valoresInseridos = new ArrayList<>();

        try {
            // Itera sobre os componentes do painel para obter os valores dos JTextFields
            for (int i = 0; i < componentes.length; i += 2) {
                if (componentes[i] instanceof JLabel && componentes[i + 1] instanceof JTextField) {
                    JLabel label = (JLabel) componentes[i];
                    JTextField campoTexto = (JTextField) componentes[i + 1];

                    String nomeColuna = label.getText();
                    String valor = campoTexto.getText();

                    // Adiciona a coluna e o valor se o campo não estiver vazio
                    if (!valor.isEmpty()) {
                        colunas.add(nomeColuna);
                        valoresInseridos.add("'" + valor + "'");
                    }
                }
            }

            // Constrói a consulta SQL dinâmica
            sql.append(String.join(", ", colunas)).append(")");
            valores.append(String.join(", ", valoresInseridos)).append(")");

            String comandoSql = sql.toString() + valores.toString();

            // Executa o comando SQL
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(comandoSql);
                jtAreaDeStatus.setText("Dados inseridos com sucesso!");
            }

        } catch (SQLException e) {
            jtAreaDeStatus.setText("Erro ao inserir dados: " + e.getMessage());
        }
    }

    /**
     * Exporta os dados de uma tabela para um arquivo CSV.
     *
     * @param nomeTabela Nome da tabela cujos dados serão exportados.
     */
    public void exportarCsv(String nomeTabela) {
        FileWriter csvWriter = null;
        try {
            // Recuperar colunas e dados da tabela
            String[] colunas = getNomeColunas(nomeTabela);
            String[][] dados = getDadosTabela(nomeTabela, colunas.length);

            // Gerar o nome do arquivo CSV
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String caminhoArquivo = "csv_exports/" + nomeTabela + "_" + timestamp + ".csv";  // Arquivo no diretório do projeto

            // Criar o FileWriter para escrever no arquivo CSV com codificação UTF-8
            csvWriter = new FileWriter(caminhoArquivo, StandardCharsets.UTF_8);

            // Escrever as colunas no arquivo CSV
            for (int i = 0; i < colunas.length; i++) {
                csvWriter.append(colunas[i]);
                if (i < colunas.length - 1) {
                    csvWriter.append(",");
                }
            }
            csvWriter.append("\n");

            // Configurar o formato de número e data de acordo com o locale da máquina
            Locale locale = Locale.getDefault(); // Usar o locale da máquina
            NumberFormat numberFormat = NumberFormat.getInstance(locale);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", locale);  // Formato de data sem hora

            // Escrever os dados no arquivo CSV
            for (String[] linha : dados) {
                for (int i = 0; i < linha.length; i++) {
                    String valor = linha[i];

                    // Verificar se o valor é numérico e formatar
                    try {
                        double num = Double.parseDouble(valor);
                        valor = numberFormat.format(num);  // Formatar com separador decimal do locale
                    } catch (NumberFormatException e) {
                        // Se não for número, verificar se é data e formatar
                        try {
                            Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(valor); // Exemplo de formato de datetime do banco
                            valor = dateFormat.format(data);  // Exportar apenas a data
                        } catch (ParseException ex) {
                            // Se não for nem número nem data, manter o valor original
                        }
                    }

                    // Adicionar o valor formatado ao CSV
                    csvWriter.append(valor);
                    if (i < linha.length - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
            }

            jtAreaDeStatus.setText("Exportação para CSV concluída: " + caminhoArquivo);

        } catch (IOException e) {
            jtAreaDeStatus.setText("Erro ao exportar para CSV: " + e.getMessage());
            Loadings.hideLoadingScreen();
        } finally {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    jtAreaDeStatus.setText("Erro ao fechar o arquivo CSV: " + e.getMessage());
                    Loadings.hideLoadingScreen();
                } finally {
                    Loadings.hideLoadingScreen();
                }
            }
        }
    }

    /**
     * Configura os campos de entrada de dados para inserção em uma tabela.
     *
     * @param painelDeInsercao JPanel onde os campos de inserção serão exibidos.
     * @param nomeTabela Nome da tabela para inserção de dados.
     */
    public void prepararInsercaoDeDados(JPanel painelDeInsercao, String nomeTabela) {
        painelDeInsercao.removeAll(); // Limpar o painel antes de adicionar novos campos

        String[] colunas = getNomeColunas(nomeTabela);

        painelDeInsercao.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Margens entre os componentes

        int row = 0;  // Contador de linhas para o GridBagLayout

        for (String coluna : colunas) {
            JLabel label = new JLabel(coluna);
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.WEST;
            painelDeInsercao.add(label, gbc); // Adiciona o JLabel para a coluna

            // Cria um JTextField para cada coluna
            JTextField campoTexto = new JTextField(20);  // Campo de texto de tamanho 20
            gbc.gridx = 1;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            painelDeInsercao.add(campoTexto, gbc); // Adiciona o JTextField para entrada de dados

            row++;  // Incrementa a linha para o próximo campo
        }

        // Adiciona um botão para realizar a inserção
        JButton btnInserir = new JButton("Inserir Dados");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        painelDeInsercao.add(btnInserir, gbc);

        // Evento para o botão de inserção
        btnInserir.addActionListener((ActionEvent e) -> {
            inserirDados(nomeTabela, painelDeInsercao); // Chama o método de inserção de dados
        });

        // Atualiza o layout do painel para garantir que os componentes sejam exibidos corretamente
        painelDeInsercao.revalidate();
        painelDeInsercao.repaint();
    }

    /**
     * Verifica se uma coluna possui restrição CHECK.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return true se houver uma restrição CHECK, caso contrário, false.
     */
    public boolean temRestricaoCheck(String nomeTabela, String coluna) {
        String query = "SELECT cc.definition FROM sys.check_constraints cc " +
                "JOIN sys.columns col ON col.object_id = cc.parent_object_id AND col.column_id = cc.parent_column_id " +
                "JOIN sys.tables tbl ON tbl.object_id = col.object_id " +
                "WHERE tbl.name = ? AND col.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString("definition").contains("IN")) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao verificar restrição CHECK: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Obtém os valores permitidos para uma coluna com restrição CHECK.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return Array de Strings com os valores permitidos.
     */
    public String[] getValoresCheck(String nomeTabela, String coluna) {
        String query = "SELECT cc.definition FROM sys.check_constraints cc " +
                "JOIN sys.columns col ON col.object_id = cc.parent_object_id AND col.column_id = cc.parent_column_id " +
                "JOIN sys.tables tbl ON tbl.object_id = col.object_id " +
                "WHERE tbl.name = ? AND col.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String condition = rs.getString("definition");
                    int start = condition.indexOf("IN (");
                    if (start != -1) {
                        int end = condition.indexOf(")", start);
                        return condition.substring(start + 4, end).replace("'", "").split(",\\s*");
                    }
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter valores do CHECK: " + ex.getMessage());
        }
        return new String[0];
    }

    /**
     * Verifica se uma coluna é chave estrangeira.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return true se a coluna for chave estrangeira, caso contrário, false.
     */
    public boolean ehChaveEstrangeira(String nomeTabela, String coluna) {
        String query = "SELECT COUNT(*) FROM sys.foreign_keys fk " +
                "JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id " +
                "JOIN sys.columns col ON col.object_id = fkc.parent_object_id AND col.column_id = fkc.parent_column_id " +
                "JOIN sys.tables tbl ON tbl.object_id = fk.parent_object_id " +
                "WHERE tbl.name = ? AND col.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao verificar chave estrangeira: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Obtém os valores das chaves estrangeiras para uma coluna.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return Array de Strings com os valores de chave estrangeira.
     */
    public String[] getValoresChaveEstrangeira(String nomeTabela, String coluna) {
        String referencedTable = null;
        String referencedColumn = null;

        // Consulta para encontrar a tabela e coluna referenciadas pela chave estrangeira
        String query = "SELECT referenced_table.name AS ReferencedTable, referenced_column.name AS ReferencedColumn " +
                "FROM sys.foreign_key_columns fk " +
                "JOIN sys.tables referenced_table ON fk.referenced_object_id = referenced_table.object_id " +
                "JOIN sys.columns referenced_column ON fk.referenced_object_id = referenced_column.object_id " +
                "AND fk.referenced_column_id = referenced_column.column_id " +
                "JOIN sys.tables parent_table ON fk.parent_object_id = parent_table.object_id " +
                "JOIN sys.columns parent_column ON fk.parent_object_id = parent_column.object_id " +
                "AND fk.parent_column_id = parent_column.column_id " +
                "WHERE parent_table.name = ? AND parent_column.name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    referencedTable = rs.getString("ReferencedTable");
                    referencedColumn = rs.getString("ReferencedColumn");
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter valores de chave estrangeira: " + ex.getMessage());
        }

        if (referencedTable == null || referencedColumn == null) {
            return new String[0];
        }

        // Consulta para obter os valores da coluna referenciada (chaves primárias da tabela referenciada)
        ArrayList<String> valoresFK = new ArrayList<>();
        String queryFKValues = "SELECT DISTINCT " + referencedColumn + " FROM " + referencedTable;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryFKValues)) {
            while (rs.next()) {
                valoresFK.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao obter valores de chave estrangeira: " + ex.getMessage());
        }

        return valoresFK.toArray(String[]::new);
    }

    /**
     * Verifica se uma coluna é chave primária.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return true se a coluna for chave primária, caso contrário, false.
     */
    public boolean ehChavePrimaria(String nomeTabela, String coluna) {
        String query = "SELECT COUNT(*) FROM sys.indexes idx " +
                "JOIN sys.index_columns ic ON idx.object_id = ic.object_id AND idx.index_id = ic.index_id " +
                "JOIN sys.columns col ON col.object_id = ic.object_id AND col.column_id = ic.column_id " +
                "JOIN sys.tables tbl ON tbl.object_id = idx.object_id " +
                "WHERE idx.is_primary_key = 1 AND tbl.name = ? AND col.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao verificar chave primária: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Verifica se uma coluna possui restrição UNIQUE.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return true se a coluna for UNIQUE, caso contrário, false.
     */
    public boolean ehColunaUnique(String nomeTabela, String coluna) {
        String query = "SELECT COUNT(*) FROM sys.indexes idx " +
                "JOIN sys.index_columns ic ON idx.object_id = ic.object_id AND idx.index_id = ic.index_id " +
                "JOIN sys.columns col ON col.object_id = ic.object_id AND col.column_id = ic.column_id " +
                "JOIN sys.tables tbl ON tbl.object_id = idx.object_id " +
                "WHERE idx.is_unique = 1 AND tbl.name = ? AND col.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomeTabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao verificar coluna UNIQUE: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Verifica se uma coluna é do tipo DATE.
     *
     * @param nomeTabela Nome da tabela.
     * @param coluna Nome da coluna.
     * @return true se a coluna for do tipo DATE, caso contrário, false.
     */
    public boolean ehColunaData(String nomeTabela, String coluna) {
        // Verifica se a coluna é do tipo DATE
        String query = "SELECT data_type FROM all_tab_columns WHERE table_name = '" + nomeTabela.toUpperCase() + "' " +
                "AND column_name = '" + coluna.toUpperCase() + "'";
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                return "DATE".equals(rs.getString(1));
            }
        } catch (SQLException e) {
            jtAreaDeStatus.setText("Erro ao verificar se a coluna é do tipo DATE: " + e.getMessage());
            Loadings.hideLoadingScreen();
        } finally {
            Loadings.hideLoadingScreen();
        }
        return false;
    }
}
