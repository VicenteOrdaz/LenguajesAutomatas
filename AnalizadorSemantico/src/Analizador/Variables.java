package Analizador;

public class Variables
{
    public static String[] tablaAnalisisSintac = { "I7	I3	I4	I5											I1	I2		I6			",
            "													P0							",
            "I8																				",
            "P3																				",
            "P4																				",
            "P5																				",
            "													P2							",
            "										I9										",
            "				I11	I12											I10				",
            "I17											I16							I13	I14	I15",
            "													P1							",
            "I18																				",
            "I7	I3	I4	I5											I19	I2		I6			",
            "					I20	I21	I22													",
            "					P11	P11	P11	I23	I24			P11								",
            "					P14	P14	P14	P14	P14			P14								",
            "I17																		I25	I14	I15",
            "					P16	P16	P16	P16	P16			P16								",
            "				I11	I12											I26				",
            "													P7							",
            "													P8							",
            "I17											I16								I27	I15",
            "																			I28	I15",
            "I17											I16									I29",
            "I17											I16									I30",
            "						I21	I22					I31								",
            "													P6							",
            "					P9	P9	P9	I23	I24			P9								",
            "					P10	P10	P10	I23	I24			P10								",
            "					P12	P12	P12	P12	P12			P12								",
            "					P13	P13	P13	P13	P13			P13								",
            "					P15	P15	P15	P15	P15			P15								" };
    public static String symbolos[] = { ",", "(", ")", "$" };
    public static String terminadorSentencia = ";";
    public static String operador_asigna = "=";
    public static String operandos[] = { "+", "-", "*", "/" };
    public static String tipos[] = { "int", "float", "char" };// "Palabras clave"

    public static String[] columnas = { "id", "int", "float", "char", ",", ";", "+", "-", "*", "/", "=", "(", ")", "$", "P", "Tipo",
            "V", "A", "E", "T", "F" };
    public static String[] filas = { "I0", "I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "I10", "I11", "I12", "I13", "I14",
            "I15", "I16", "I17", "I18", "I19", "I20", "I21", "I22", "I23", "I24", "I25", "I26", "I27", "I28", "I29",
            "I30", "I31" };

    public static String[] produciones={"P","Tipo","A","int","float","char",",",";","id","E","E", "T", "T","T", "F","(","id"};
    public static String[] agregaP={"P'","P","P","Tipo","Tipo","Tipo","V","V","A","E","E","E","T","T","T","F","F"};
    public static String[] auxP={"P0","P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12","P13","P14","P15","P16"};
    public static String[] especiales = {"","P9", "P10", "P12", "P13"};


}
