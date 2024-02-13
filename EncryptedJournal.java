import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedJournal extends JFrame {
    private JTextArea textArea;
    private SecretKey secretKey;
    private final String ENCRYPTION_ALGORITHM = "AES";

    public EncryptedJournal() {
        initializeUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initializeUI() {
        setTitle("Journal Window");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton encryptButton = new JButton("Encrypt and Save");
        encryptButton.addActionListener(e -> {
            String enteredKey = promptForEncryptionKey();
            if (enteredKey != null) {
                encryptAndSave(textArea.getText(), enteredKey);
            }
        });

        JButton decryptButton = new JButton("Decrypt");
        decryptButton.addActionListener(e -> {
            String enteredKey = promptForEncryptionKey();
            if (enteredKey != null) {
                String decryptedText = decrypt(enteredKey);
                openDecryptedTextWindow(decryptedText);
            }
        });

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, scrollPane);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(decryptButton);
        buttonPanel.add(encryptButton);
        add(BorderLayout.SOUTH, buttonPanel);
        setVisible(true);
    }

    private SecretKey generateKeyFromInput(int userInput) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(String.valueOf(userInput).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error generating key from input");
            return null;
        }
    }

    private void encryptAndSave(String plainText, String encryptionKey) {
        try {
            int userInput = Integer.parseInt(encryptionKey);
            secretKey = generateKeyFromInput(userInput);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);

            // Save the file to the desktop
            String desktopPath = System.getProperty("user.home") + "/Desktop/";
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_hhmmss");
            String fileName = desktopPath + "entry" + "_" + dateFormat.format(new Date()) + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(encryptedText);
            }

            JOptionPane.showMessageDialog(this, "Text encrypted and saved successfully to " + fileName);
            initializeUI(); // Return to the journal window
        } catch (NumberFormatException | IOException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error encrypting and saving text");
            initializeUI();
        }
    }

    private void openDecryptedTextWindow(String decryptedText) {
        JFrame decryptedTextFrame = new JFrame("Decrypted Text");
        decryptedTextFrame.setSize(400, 300);
        decryptedTextFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        decryptedTextFrame.setLocationRelativeTo(null);

        JTextArea decryptedTextArea = new JTextArea(decryptedText);
        decryptedTextArea.setEditable(false);
        decryptedTextArea.setLineWrap(true);
        decryptedTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(decryptedTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            decryptedTextFrame.dispose();
        });

        decryptedTextFrame.getRootPane().setDefaultButton(exitButton);

        decryptedTextFrame.setLayout(new BorderLayout());
        decryptedTextFrame.add(BorderLayout.CENTER, scrollPane);
        decryptedTextFrame.add(BorderLayout.SOUTH, exitButton);

        decryptedTextFrame.setVisible(true);
    }

    private String decrypt(String encryptionKey) {
        try {
            int userInput = Integer.parseInt(encryptionKey);
            secretKey = generateKeyFromInput(userInput);

            // Allow the user to choose the file to decrypt
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose Encrypted File to Decrypt");
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                byte[] encryptedBytes = Files.readAllBytes(selectedFile.toPath());
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);

                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedBytes));
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            }
        } catch (NumberFormatException | IOException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            ((Throwable) e).printStackTrace();
            JOptionPane.showMessageDialog(this, "Error decrypting text");
            initializeUI();
        }
        return "";
    }

    private String promptForEncryptionKey() {
        return JOptionPane.showInputDialog("Enter an integer encryption key:");
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EncryptedJournal app = new EncryptedJournal();
            app.setVisible(true);
        });
    }
}
