package compiler;

import java.io.*;
import javax.swing.JOptionPane;


public class Serializa 
{
	public static void saveObject(Object obj, File name)
	{
		try 
		{
                    ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(name));
		      salida.writeObject(obj);
		      salida.close();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(null, "error al guardar");
			e.printStackTrace();
		}
	      
	}


	@SuppressWarnings("resource")
	public  static Object writeObject(InputStream in)
	{
		    ObjectInputStream entrada=null;
		    
		    try {
				entrada = new ObjectInputStream(in);
				return entrada.readObject();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Error al leer el archivo: "+e.getMessage());
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("Class not found exception");
                                e.printStackTrace();
                                System.exit(0);
			}
				
				
				
			
			return null;
	}
}
