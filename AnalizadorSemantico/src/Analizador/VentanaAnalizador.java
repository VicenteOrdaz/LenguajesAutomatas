package Analizador;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class VentanaAnalizador extends JFrame {

    JTextArea errores;
    JTextPane codigoFuente;
    JSplitPane division;
    JMenuBar bar;
    public File current;

    public VentanaAnalizador() throws HeadlessException {
        super("Analizador sintactico - semantico -- Julio | Vicente");

        bar = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");

        JMenuItem abrir = new JMenuItem("Abrir");
        JMenuItem save = new JMenuItem("Guardar");
        JMenuItem analisis = new JMenuItem("Analizar");
        analisis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));


        archivo.add(abrir);
        archivo.add(save);
        archivo.add(analisis);

        bar.add(archivo);

        analisis.addActionListener(a->{
            Analisis obj = new Analisis();
            obj.separar(codigoFuente.getText().replace
                    ("\t"," ").replaceAll
                    ("\n"," "),errores);

            try {
                obj.analizar();
            }catch(Exception e){
                errores.setForeground(Color.red);
                errores.setText("Cadena no aceptada");
            }
        });

        abrir.addActionListener(a->{
            JFileChooser jf = new JFileChooser();
            jf.setFileFilter(new FileNameExtensionFilter("Archivos txt","txt"));

            if(jf.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                current = jf.getSelectedFile();

                try {
                    FileReader fr = new FileReader(current);
                    BufferedReader br = new BufferedReader(fr);
                    String cad;
                    StringBuilder sb = new StringBuilder();

                    while ((cad = br.readLine()) != null) {
                        sb.append(cad).append("\n");
                    }

                    codigoFuente.setText(sb.toString());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });

        abrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

        save.addActionListener(a->{

            if(current==null)
            {
                JFileChooser j = new JFileChooser();
                if(j.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                    current = j.getSelectedFile();
                    write();
                }
            } else write();
        });
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        setJMenuBar(bar);

        getContentPane().setLayout(new BorderLayout());

        division = new JSplitPane(JSplitPane.VERTICAL_SPLIT);


        codigoFuente = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(codigoFuente);
        TextLineNumber textLineNumber = new TextLineNumber(codigoFuente);
        scrollPane.setRowHeaderView(textLineNumber);

        codigoFuente.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 18));

        errores = new JTextArea();
        errores.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 18));

        division.setTopComponent(scrollPane);
        division.setBottomComponent(new JScrollPane(errores));
        division.setResizeWeight(0.5d);

        getContentPane().add(division);


        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    void write(){
        try {
            FileWriter fr = new FileWriter(current);
            BufferedWriter br = new BufferedWriter(fr);

            br.write(codigoFuente.getText());
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            new VentanaAnalizador().setVisible(true);
        });
    }
}

