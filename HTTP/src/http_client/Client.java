import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private final JFrame frame;
    private final JTextField urlField;
    private final JTextArea contentArea;
    private final JTextField postDataField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }

    public Client() {
        frame = new JFrame("Simple HTTP Client");
        urlField = new JTextField(30);
        contentArea = new JTextArea(20, 40);
        postDataField = new JTextField(30);
        contentArea.setEditable(false);

        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(this::fetchContent);

        JButton postButton = new JButton("Post");
        postButton.addActionListener(this::postContent);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); // Add some padding

        // URL Label
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("URL:"), c);

        // URL Field
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(urlField, c);

        // Fetch Button
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panel.add(fetchButton, c);

        // Post Data Label
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Post Data:"), c);

        // Post Data Field
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(postDataField, c);

        // Post Button
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panel.add(postButton, c);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void fetchContent(ActionEvent event) {
        String urlString = urlField.getText();
        if (!urlString.startsWith("http://")) {
            urlString = "http://" + urlString;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();
                contentArea.setText(content.toString());
            } else if (responseCode == 404) {
                contentArea.setText("Error 404: File not found");
            }
        } catch (IOException e) {
            contentArea.setText("Error: " + e.getMessage());
        }
    }

    private void postContent(ActionEvent event) {
        String urlString = urlField.getText();
        String postData = postDataField.getText();
        if (!urlString.startsWith("http://")) {
            urlString = "http://" + urlString;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                contentArea.setText("Data posted successfully.");
            } else {
                contentArea.setText("Error: " + responseCode);
            }
        } catch (IOException e) {
            contentArea.setText("Error: " + e.getMessage());
        }
    }
}
