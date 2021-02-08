package Analizador;
import javax.swing.*;
import java.awt.*;
import java.util.*;

import static Analizador.Variables.*;

public class Analisis {

	Vector<String[]> vectb = new Vector<String[]>();
	Stack<String> pila = new Stack<String>();
	Stack<String[]> GuardarDatos = new Stack<String[]>();
	Stack<String> PilaSem = new Stack<String>();
	String T1,T2;
	String[] vect;
	JTextArea error;

	public void separar(String cad, JTextArea error) {

		this.error = error;
		vect = cad.concat(" $").split("\\s+");

		for (int i = 0; i < tablaAnalisisSintac.length; i++) {
			vectb.add(i, tablaAnalisisSintac[i].split((char) 9 + "", 50));
		}
		error.setForeground(Color.green);
	}

	// Sintactico
	public void analizar() {
		pila.push("I0");
		String cadena = "";

		int fila = 0;
		int col = 0;
		int i = 0;
		String IoP = "";
		System.out.println(Bposicion(especiales, "P9"));
		do {

			cadena = Retornar(vect[i]);
			fila = Bposicion(filas, pila.elementAt(pila.size() - 1));
			col = Bposicion(columnas, cadena);
			IoP = vectb.elementAt(fila)[col].charAt(0) + "";
			if(cadena.equalsIgnoreCase("$")&&vectb.elementAt(fila)[col].equalsIgnoreCase("P0")){
				error.setText("Cadena aceptada");
				break;
			}
			System.out.println(" Fila:"+fila+" | Columna:"+col+" | Caracter:"+cadena+" | Tabla:"+vectb.elementAt(fila)[col]);
			System.out.println(pila);

			if (IoP.equals("I")) {    /* ES UN DESPLAZAMIENTO */
				pila.push(cadena);
				pila.push(vectb.elementAt(fila)[col]);/* la pila agrega la cadena con su estado */
				i++;
				System.out.println(" Fila:"+fila+" Columna:"+col+" Caracter:"+cadena+" Tabla:"+vectb.elementAt(fila)[col]);
				System.out.println(pila);

			} else if (IoP.equals("P")) {       /* es produccion/reduccion */
				System.out.println(" Fila:" + fila + " Columna:" + col + " Caracter:" + cadena + " Tabla:" + vectb.elementAt(fila)[col]);
				System.out.println(pila);
				String auxvectb = vectb.elementAt(fila)[col];
				System.out.println(auxvectb);
				do {
					pila.pop();
					System.out.println(" Fila:" + fila + " Columna:" + col + " Caracter:" + cadena + " Tabla:" + vectb.elementAt(fila)[col]);
					System.out.println(pila);
				} while (!produciones[Bposicion(auxP, vectb.elementAt(fila)[col])].equalsIgnoreCase(pila.elementAt(pila.size() - 1)));
				//hasta donde se elimina					de la pila
				pila.pop();
				pila.push(agregaP[Bposicion(auxP, vectb.elementAt(fila)[col])]);
				//se le agrega a la pila la produccion
				fila = Bposicion(filas, pila.elementAt(pila.size() - 2));
				col = Bposicion(columnas, pila.elementAt(pila.size() - 1));
				pila.push(vectb.elementAt(fila)[col]);
				System.out.println(" Fila:" + fila + " Columna:" + col + " Caracter:" + cadena + " Tabla:" + vectb.elementAt(fila)[col]);
				System.out.println(pila);

				if (auxvectb == "P16") {
					System.out.println(PilaSem);
				} else if (Bposicion(especiales, auxvectb) != 0) {

					T1 = PilaSem.pop();
					T2 = PilaSem.pop();

					if (!T1.equalsIgnoreCase("char") && !T2.equalsIgnoreCase("char")) {
						if (!T1.equalsIgnoreCase(T2)) {
							PilaSem.push("float");
							System.out.println(PilaSem);
						} else {
							PilaSem.push(T1);
							System.out.println("Pila Semántica: " + PilaSem);
						}
					} else {
						//error semantico
						error.setText("Cadena no aceptada - error semántico");
						error.setForeground(Color.red);
						break;
					}
				} else if (auxvectb.equalsIgnoreCase("P8")) {
					T1 = PilaSem.pop();
					T2 = PilaSem.pop();
					if (!T1.equalsIgnoreCase(T2)) {
						error.setText("Cadena no aceptada - error semántico");
						error.setForeground(Color.red);
						break;
					}
					System.out.println(PilaSem);
				} else {
					System.out.println(PilaSem);
				}
			}
		} while (i < vect.length);
	}

	String dato = "";
	boolean ban_asigna = false;

	// Lexico
	public String Retornar(String palabra) {

		if (!BvecString(tipos, palabra).equalsIgnoreCase("")) {
			dato = palabra;
			return palabra;
		} else {
			// int a;
			if (palabra.equalsIgnoreCase(terminadorSentencia)) {
				dato = "";
				if (ban_asigna) {

					ban_asigna = false;
				}
			} else if (!BvecString(operandos, palabra).equalsIgnoreCase("")) {

				return palabra;
			} else if (palabra.equalsIgnoreCase(operador_asigna)) {
				ban_asigna = true;
				return palabra;

			} else if (palabra.matches("[A-z]+[0-9]*[A-z]*")) {// Es un id
				if(dato.equalsIgnoreCase("")){
					T1=BuscarDatos(palabra);
					PilaSem.push(T1);
				}
				String[] auxvec = { palabra, dato, "0" };
				GuardarDatos.push(auxvec);
				return "id";
			} else {
				return palabra;
			}
		}
		return palabra;
	}

	public String BuscarDatos(String dat){
		for (int i = 0; i < GuardarDatos.size(); i++) {
			if(GuardarDatos.elementAt(i)[0].equalsIgnoreCase(dat)){
				return GuardarDatos.elementAt(i)[1];
			}
		}
		return "";
	}

	public int Bposicion(String cadenas[], String buscar) {
		for (int i = 0; i < cadenas.length; i++) {
			if (cadenas[i].equalsIgnoreCase(buscar)) {
				return i;
			}
		}
		return 0;
	}

	// Tipo
	public String BvecString(String cadenas[], String buscar) {
		for (int i = 0; i < cadenas.length; i++) {
			if (cadenas[i].equalsIgnoreCase(buscar)) {
				return cadenas[i];
			}
		}
		return "";
	}
}







