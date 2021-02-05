package Principal;

import Semantico.HijaSemantico;
import compiler.Analisis;
import compiler.Compiler;
import test.Test;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class Principal extends JFrame
{
    public JTextPane textAreaCodigo;
    public JTextArea textAreaAnalisis;
    public JTextArea textAreaCodigoObjeto;
    public TextLineNumber textLineNumber;
    public Compiler compiladorGeneral;
    public JMenuBar menuBar;
    public File current;
    public HijaSemantico hijaSemantico;

    boolean creado;

    private void semanticoError(){

    }

    private void semanticoAceptado(){

    }

    private void sintaxisAceptada(){

    }

    private void fuente(JTextComponent txt){
        txt.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));
    }

    public Principal()
    {
        menuBar = new JMenuBar();

        hijaSemantico = new HijaSemantico(this::semanticoError,this::semanticoAceptado);

        JMenu archivo = new JMenu("Archivo");

        JMenuItem abrir = new JMenuItem("Abrir");
        JMenuItem save = new JMenuItem("Guardar");
        JMenuItem analisis = new JMenuItem("Analizar");
        analisis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

        analisis.addActionListener(a->{

            if(!creado)
            {
                compiladorGeneral = new Compiler(hijaSemantico,this::sintaxisAceptada);
                creado = true;
            }

            Analisis.perfomance(textAreaCodigo.getText());
            String errLex = "Errores lexicos:\n"+compiladorGeneral.erroresLexicos;
            String errSint = "\nErrores sintacticos:\n"+compiladorGeneral.erroresSintacticos;
            String errSeman = "\nErrores semantico:\n"+hijaSemantico.erroresSemantico;
            textAreaAnalisis.setText(errLex+errSint+errSeman);
            textAreaCodigoObjeto.setText(hijaSemantico.traductoCodigoC.getCode());
        });

        abrir.addActionListener(a->{
            JFileChooser jf = new JFileChooser();
            jf.setFileFilter(new FileNameExtensionFilter("Archivos cao","cao","CAO"));

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

                        textAreaCodigo.setText(sb.toString());

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

        archivo.add(abrir);
        archivo.add(save);
        archivo.add(analisis);

        menuBar.add(archivo);


        setJMenuBar(menuBar);

        getContentPane().setLayout(new GridLayout(1,3,15,15));

        textAreaCodigo = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textAreaCodigo);
        textLineNumber = new TextLineNumber(textAreaCodigo);
        scrollPane.setRowHeaderView( textLineNumber );

        textAreaAnalisis = new JTextArea();
        textAreaCodigoObjeto = new JTextArea();

        getContentPane().add(scrollPane);
        getContentPane().add(new JScrollPane(textAreaAnalisis));
        getContentPane().add(new JScrollPane(textAreaCodigoObjeto));

        fuente(textAreaCodigo);
        fuente(textAreaAnalisis);
        fuente(textAreaCodigoObjeto);


        setSize(900,900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

    }

    void write(){
        try {
            FileWriter fr = new FileWriter(current);
            BufferedWriter br = new BufferedWriter(fr);

            br.write(textAreaCodigo.getText());
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Principal();

            }
        });

//        Test.test("public static void principal()\n" +
//                "inicio fin");

    }
}
