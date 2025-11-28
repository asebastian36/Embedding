package com.embedding.code;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {

    // Instancia del servicio (Lógica separada)
    private final Word2VecService service;

    // Componentes UI
    private JTextArea txtCorpus;
    private JTextArea txtLog;
    private JProgressBar barraProgreso;

    // Inputs
    private JTextField txtP1, txtP2;
    private JTextField txtF1, txtF2;
    private JButton btnCargar, btnEntrenar;

    // Fuentes y Colores (Constantes)
    private final Font FONT_STD = new Font("SansSerif", Font.PLAIN, 14);
    private final Color COL_BG = new Color(245, 245, 245);

    public MainFrame() {
        super("Práctica Embeddings - Interpretación y Validación");
        this.service = new Word2VecService();

        configurarEstilos();
        inicializarUI();

        this.setSize(1100, 800);
        this.setMinimumSize(new Dimension(800, 600)); // Evita que la ventana se haga demasiado pequeña
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        log("Sistema iniciado.");
        log("NOTA: Se requiere un corpus de al menos 1000 caracteres para entrenar.");
    }

    private void inicializarUI() {
        // LAYOUT MAESTRO: GridBagLayout es el único que respeta tamaños al redimensionar
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(COL_BG);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; // Todo ocupa el ancho completo

        // --- 1. ZONA SUPERIOR: PESTAÑAS (60% de la altura) ---
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        mainPanel.add(crearPanelPestanas(), gbc);

        // --- 2. ZONA CENTRAL: CALCULADORAS (Altura fija/mínima) ---
        gbc.gridy = 1;
        gbc.weighty = 0.0; // IMPORTANTE: 0.0 evita que este panel se estire o se aplaste
        gbc.insets = new Insets(10, 0, 10, 0); // Margen arriba y abajo
        mainPanel.add(crearPanelCalculadoras(), gbc);

        // --- 3. ZONA INFERIOR: LOGS (40% de la altura restante) ---
        gbc.gridy = 2;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(crearPanelLogs(), gbc);

        this.setContentPane(mainPanel);
    }

    // ==========================================
    //       MÓDULOS DE INTERFAZ (UI)
    // ==========================================

    private JTabbedPane crearPanelPestanas() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Módulo Fase 1
        JPanel p1 = new JPanel(new GridBagLayout());
        p1.setBackground(Color.WHITE);
        btnCargar = new JButton("CARGAR MODELO PRE-ENTRENADO (.vec)");
        btnCargar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCargar.setPreferredSize(new Dimension(300, 50));
        btnCargar.addActionListener(e -> accionCargarModelo());
        p1.add(btnCargar);

        // Módulo Fase 2
        JPanel p2 = new JPanel(new BorderLayout(10, 10));
        p2.setBackground(Color.WHITE);
        p2.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("Corpus de Entrenamiento (>1000 caracteres):");
        lbl.setFont(FONT_STD);

        txtCorpus = new JTextArea();
        txtCorpus.setFont(FONT_STD);
        txtCorpus.setLineWrap(true);
        txtCorpus.setWrapStyleWord(true);

        btnEntrenar = new JButton("INICIAR ENTRENAMIENTO LOCAL");
        btnEntrenar.setBackground(new Color(220, 255, 220));
        btnEntrenar.addActionListener(e -> accionEntrenar());

        p2.add(lbl, BorderLayout.NORTH);
        p2.add(new JScrollPane(txtCorpus), BorderLayout.CENTER);
        p2.add(btnEntrenar, BorderLayout.SOUTH);

        tabs.addTab(" Fase 1: Cargar ", p1);
        tabs.addTab(" Fase 2: Entrenar ", p2);

        return tabs;
    }

    private JPanel crearPanelCalculadoras() {
        // Usamos GridLayout de 2 filas con espacio vertical de 10px
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 10));
        p.setOpaque(false);

        // Fila 1: Palabras
        JPanel rowPalabras = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        rowPalabras.setBackground(Color.WHITE);
        rowPalabras.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Similitud Palabras"));

        txtP1 = new JTextField(12);
        txtP2 = new JTextField(12);
        estilizarInput(txtP1); estilizarInput(txtP2);

        JButton btnCalcP = new JButton("Calcular");
        btnCalcP.addActionListener(e -> accionSimilitudPalabras());

        rowPalabras.add(new JLabel("Palabra A:")); rowPalabras.add(txtP1);
        rowPalabras.add(new JLabel("Palabra B:")); rowPalabras.add(txtP2);
        rowPalabras.add(btnCalcP);

        // Fila 2: Frases
        JPanel rowFrases = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        rowFrases.setBackground(Color.WHITE);
        rowFrases.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Similitud Frases (Fase 3)"));

        txtF1 = new JTextField(20);
        txtF2 = new JTextField(20);
        estilizarInput(txtF1); estilizarInput(txtF2);

        JButton btnCalcF = new JButton("Calcular");
        btnCalcF.addActionListener(e -> accionSimilitudFrases());

        rowFrases.add(new JLabel("Frase 1:")); rowFrases.add(txtF1);
        rowFrases.add(new JLabel("Frase 2:")); rowFrases.add(txtF2);
        rowFrases.add(btnCalcF);

        p.add(rowPalabras);
        p.add(rowFrases);

        return p;
    }

    private JPanel crearPanelLogs() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Registros y Logs"));
        p.setBackground(COL_BG);

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Esperando...");

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));

        p.add(barraProgreso, BorderLayout.NORTH);
        p.add(new JScrollPane(txtLog), BorderLayout.CENTER);
        return p;
    }

    private void estilizarInput(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txt.setMargin(new Insets(5, 5, 5, 5)); // Hace la caja más "gorda"
    }

    // ==========================================
    //          LÓGICA DE EVENTOS
    // ==========================================

    private void accionCargarModelo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Vectores", "vec", "bin", "txt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            ejecutarEnBackground("Cargando modelo...", () -> {
                try {
                    service.cargarModeloExterno(f);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                log("Modelo cargado exitosamente.");
                log("Vocabulario disponible: " + service.getVocabSize());
            });
        }
    }

    private void accionEntrenar() {
        String texto = txtCorpus.getText();

        // VALIDACIÓN: 1000 CARACTERES MÍNIMOS
        if (texto.length() < 1000) {
            JOptionPane.showMessageDialog(this,
                    "El corpus es demasiado pequeño (" + texto.length() + " caracteres).\n" +
                            "Necesitas al menos 1000 caracteres para evitar resultados pobres.",
                    "Corpus Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ejecutarEnBackground("Entrenando Word2Vec...", () -> {
            service.entrenarModeloLocal(texto);
            log("Entrenamiento finalizado.");
            log("Vocabulario aprendido: " + service.getVocabSize());
            log("Modelo listo para usar.");
        });
    }

    private void accionSimilitudPalabras() {
        if (!service.isModeloCargado()) { log("Error: Carga un modelo primero."); return; }
        try {
            String p1 = txtP1.getText().trim().toLowerCase();
            String p2 = txtP2.getText().trim().toLowerCase();

            double sim = service.calcularSimilitudPalabras(p1, p2);

            if (sim == -999) {
                log("Error: Alguna palabra no existe en el vocabulario.");
            } else {
                imprimirResultado("Palabras", "'" + p1 + "' vs '" + p2 + "'", sim);
            }
        } catch (Exception e) { log("Error: " + e.getMessage()); }
    }

    private void accionSimilitudFrases() {
        if (!service.isModeloCargado()) { log("Error: Carga un modelo primero."); return; }
        try {
            String f1 = txtF1.getText();
            String f2 = txtF2.getText();

            double sim = service.calcularSimilitudFrases(f1, f2);

            if (sim == -999) {
                log("Error: Frases vacías o desconocidas.");
            } else {
                imprimirResultado("Frases", "Frase 1 vs Frase 2", sim);
            }
        } catch (Exception e) { log("Error: " + e.getMessage()); }
    }

    // ==========================================
    //       UTILIDADES DE INTERPRETACIÓN
    // ==========================================

    private void imprimirResultado(String tipo, String detalle, double sim) {
        String interpretacion = interpretarSimilitud(sim);

        log("--------------------------------------------------");
        log("CÁLCULO: " + tipo);
        log("Detalle: " + detalle);
        log(String.format("SCORE:   %.4f", sim));
        log("INTERPRETACIÓN: " + interpretacion);
        log("--------------------------------------------------");
    }

    // LÓGICA DE INTERPRETACIÓN (SEMÁFORO)
    private String interpretarSimilitud(double sim) {
        if (sim > 0.8) return "MUY SIMILARES (Conceptos casi idénticos)";
        if (sim > 0.5) return "SIMILARES (Contexto relacionado)";
        if (sim > 0.2) return "ALGO RELACIONADOS (Mismo tema)";
        if (sim > -0.2) return "DIFERENTES (Poca o nula relación)";
        return "OPUESTOS / EXCLUYENTES (Contextos contrarios)";
    }

    private void ejecutarEnBackground(String mensaje, Runnable tarea) {
        barraProgreso.setIndeterminate(true);
        barraProgreso.setString(mensaje);
        btnCargar.setEnabled(false);
        btnEntrenar.setEnabled(false);

        new Thread(() -> {
            try {
                tarea.run();
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setIndeterminate(false);
                    barraProgreso.setString("Listo");
                    barraProgreso.setValue(100);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setIndeterminate(false);
                    barraProgreso.setString("Error");
                    log("Error crítico: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnCargar.setEnabled(true);
                    btnEntrenar.setEnabled(true);
                });
            }
        }).start();
    }

    private void log(String m) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(m + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private void configurarEstilos() {
        try {
            // Forzar estilo Metal para evitar bugs de Linux/GTK
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TextField.background", new ColorUIResource(Color.WHITE));
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}