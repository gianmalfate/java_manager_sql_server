package project;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.SwingWorker;

/**
 *
 * @author driicarvalho7
 */
public class JanelaPrincipal {

    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JComboBox<String> jc;
    JTextArea jtAreaDeStatus;
    JTextArea taDDLSchema;
    JTabbedPane tabbedPane;
    JPanel pPainelDeExibicaoDeDados;
    JTable jt;
    JPanel pPainelDeInsecaoDeDados;
    JPanel pPainelDoDDLSchema;
    DBFuncionalidades bd;
    private JButton btnExportarCSV;
    private JButton btnExibirDDL;
    String nomeTabela = "";

    /**
     * Cria todos os elementos (Buttons, JPanel, JFrame etc...) necessários para manipular o sistema.
     */
    public void ExibeJanelaPrincipal() {
        j = new JFrame("ICMC-USP - LAB BD");
        j.setSize(1000, 650);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setLocationRelativeTo(null);

        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox<>();
        jc.addItem("- SELECIONAR -");
        pPainelDeCima.add(jc);

        btnExportarCSV = new JButton("Exporta CSV MSExcel");
        pPainelDeCima.add(btnExportarCSV);

        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

        pPainelDeExibicaoDeDados = new JPanel(new GridLayout(1, 1));
        tabbedPane.add(pPainelDeExibicaoDeDados, "Exibição");

        jt = new JTable(new DefaultTableModel());
        JScrollPane jsp = new JScrollPane(jt);
        pPainelDeExibicaoDeDados.add(jsp);

        pPainelDeInsecaoDeDados = new JPanel(new GridLayout(0, 2));
        tabbedPane.add(pPainelDeInsecaoDeDados, "Inserção");

        pPainelDoDDLSchema = new JPanel(new BorderLayout());
        taDDLSchema = new JTextArea();
        taDDLSchema.setEditable(false);
        JScrollPane scrollDDL = new JScrollPane(taDDLSchema);
        pPainelDoDDLSchema.add(scrollDDL, BorderLayout.CENTER);
        btnExibirDDL = new JButton("Exibir DDL do Schema");
        pPainelDoDDLSchema.add(btnExibirDDL, BorderLayout.SOUTH);
        tabbedPane.add(pPainelDoDDLSchema, "DDL do Schema");

        j.setVisible(true);

        bd = new DBFuncionalidades(jtAreaDeStatus);
        if (bd.conectar()) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    bd.pegarNomesDeTabelas(jc);
                    return null;
                }

                @Override
                protected void done() {
                    Loadings.hideLoadingScreen();
                }
            }.execute();
        }

        this.DefineEventos();
    }

    /**
     * ActionListiners que realizam ações em na classe 'DBFuncionalidades.java' de acordo com a ação do usuárioo.
     */
    private void DefineEventos() {
        jc.addActionListener((ActionEvent e) -> {
            if (jc.getSelectedIndex() <= 0) return;
            Loadings.showLoadingScreen(j);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    nomeTabela = (String) jc.getSelectedItem();
                    jtAreaDeStatus.setText("Tabela selecionada: " + nomeTabela);
                    bd.exibeDados(jt, nomeTabela);
                    bd.prepararInsercaoDeDados(pPainelDeInsecaoDeDados, nomeTabela);
                    return null;
                }

                @Override
                protected void done() {
                    Loadings.hideLoadingScreen();
                }
            }.execute();
        });

        btnExportarCSV.addActionListener((ActionEvent e) -> {
            if (nomeTabela == null || nomeTabela.isEmpty() || "- SELECIONAR -".equals(nomeTabela)) {
                jtAreaDeStatus.setText("Nenhuma tabela selecionada.");
                return;
            }
            Loadings.showLoadingScreen(j);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    bd.exportarCsv(nomeTabela);
                    return null;
                }

                @Override
                protected void done() {
                    Loadings.hideLoadingScreen();
                }
            }.execute();
        });

        btnExibirDDL.addActionListener((ActionEvent e) -> {
            Loadings.showLoadingScreen(j);

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return bd.getDDLTables();
                }

                @Override
                protected void done() {
                    try {
                        taDDLSchema.setText(get());
                    } catch (InterruptedException | ExecutionException ex) {
                        jtAreaDeStatus.setText("Erro ao exibir DDL: " + ex.getMessage());
                    } finally {
                        Loadings.hideLoadingScreen();
                    }
                }
            }.execute();
        });
    }
}
