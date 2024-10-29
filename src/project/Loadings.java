package project;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author driicarvalho7
 */
public class Loadings {

    private static JDialog loadingDialog;
    private static JLabel loadingLabel;
    private static Timer timer;

    /**
     * Exibe um modal de 'carregando...' enquanto o sistema realiza conexão com o banco de dados.
     *
     * @param parentFrame JFrame que está sendo exibido no momento do loading.
     */
    public static void showLoadingScreen(JFrame parentFrame) {
        if (loadingDialog != null && loadingDialog.isVisible()) {
            return; // Não cria uma nova tela se já estiver aberta
        }

        loadingDialog = new JDialog(parentFrame, "", true);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(parentFrame);

        loadingLabel = new JLabel("Carregando", JLabel.CENTER);
        loadingDialog.add(loadingLabel);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        timer = new Timer(500, (ActionEvent e) -> {
            String currentText = loadingLabel.getText();
            switch (currentText) {
                case "Carregando" -> loadingLabel.setText("Carregando.");
                case "Carregando." -> loadingLabel.setText("Carregando..");
                case "Carregando.." -> loadingLabel.setText("Carregando...");
                default -> loadingLabel.setText("Carregando");
            }
        });
        timer.start();

        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
    }

    /**
     * Fecha o modal que foi criado acima
     */
    public static void hideLoadingScreen() {
        if (timer != null) {
            timer.stop();
        }
        if (loadingDialog != null) {
            loadingDialog.dispose();
        }
    }
}

