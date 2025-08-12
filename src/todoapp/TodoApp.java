package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class TodoApp extends JFrame implements ActionListener {
    private final JTextField taskField;
    private final JButton addButton;
    private final JButton deleteButton;
    private final DefaultListModel<String> listModel;
    private final JList<String> taskList;
    private static final String FILE_NAME = "tasks.txt";

    public TodoApp() {
        super("To-Do App (Java Swing)");

        // --- Top panel ---
        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        taskField = new JTextField();
        addButton = new JButton("Add Task");
        topPanel.add(taskField, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        // --- Center ---
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Tasks"));

        // --- Bottom ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = new JButton("Delete Selected");
        bottomPanel.add(deleteButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        // Layout
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        taskField.addActionListener(e -> addTask());
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = taskList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        String s = listModel.get(idx);
                        if (s.startsWith("[DONE] ")) {
                            listModel.set(idx, s.substring(7));
                        } else {
                            listModel.set(idx, "[DONE] " + s);
                        }
                        saveTasks();
                    }
                }
            }
        });

        // Load tasks from file at startup
        loadTasks();

        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 500);
        setLocationRelativeTo(null);

        // Save tasks when closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addTask();
        } else if (e.getSource() == deleteButton) {
            deleteSelectedTasks();
        }
    }

    private void addTask() {
        String text = taskField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a task before adding.", "Empty task", JOptionPane.WARNING_MESSAGE);
            return;
        }
        listModel.addElement(text);
        taskField.setText("");
        taskField.requestFocus();
        saveTasks();
    }

    private void deleteSelectedTasks() {
        int[] selected = taskList.getSelectedIndices();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more tasks to delete.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (int i = selected.length - 1; i >= 0; i--) {
            listModel.remove(selected[i]);
        }
        saveTasks();
    }

    private void saveTasks() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_NAME))) {
            for (int i = 0; i < listModel.size(); i++) {
                writer.write(listModel.get(i));
                writer.newLine();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        Path filePath = Paths.get(FILE_NAME);
        if (Files.exists(filePath)) {
            try {
                List<String> lines = Files.readAllLines(filePath);
                for (String task : lines) {
                    if (!task.trim().isEmpty()) {
                        listModel.addElement(task);
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TodoApp().setVisible(true);
        });
    }
}