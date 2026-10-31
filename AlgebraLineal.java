import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Font;

public class AlgebraLineal {

    // Excepcion para cancelar el ingreso de datos.
    static class CancelarEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    // =====================================================
    // FRACCIONES EXACTAS
    // =====================================================

    static class Fraccion {
        BigInteger n;
        BigInteger d;

        Fraccion(long valor) {
            this(BigInteger.valueOf(valor), BigInteger.ONE);
        }

        Fraccion(BigInteger numerador, BigInteger denominador) {
            if (denominador.signum() == 0) {
                throw new ArithmeticException(
                    "El denominador no puede ser cero."
                );
            }

            if (denominador.signum() < 0) {
                numerador = numerador.negate();
                denominador = denominador.negate();
            }

            BigInteger mcd = numerador.gcd(denominador);

            n = numerador.divide(mcd);
            d = denominador.divide(mcd);
        }

        static Fraccion leer(String texto) {
            texto = texto.trim();

            if (texto.isEmpty()) {
                throw new NumberFormatException();
            }

            if (texto.contains("/")) {
                String[] partes = texto.split("/", -1);

                if (partes.length != 2) {
                    throw new NumberFormatException();
                }

                return new Fraccion(
                    new BigInteger(partes[0].trim()),
                    new BigInteger(partes[1].trim())
                );
            }

            BigDecimal decimal = new BigDecimal(texto);

            BigInteger numerador = decimal.unscaledValue();
            int escala = decimal.scale();

            if (escala < 0) {
                return new Fraccion(
                    numerador.multiply(BigInteger.TEN.pow(-escala)),
                    BigInteger.ONE
                );
            }

            return new Fraccion(
                numerador,
                BigInteger.TEN.pow(escala)
            );
        }

        Fraccion sumar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.d).add(otra.n.multiply(d)),
                d.multiply(otra.d)
            );
        }

        Fraccion restar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.d).subtract(otra.n.multiply(d)),
                d.multiply(otra.d)
            );
        }

        Fraccion multiplicar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.n),
                d.multiply(otra.d)
            );
        }

        Fraccion dividir(Fraccion otra) {
            if (otra.esCero()) {
                throw new ArithmeticException(
                    "No se puede dividir entre cero."
                );
            }

            return new Fraccion(
                n.multiply(otra.d),
                d.multiply(otra.n)
            );
        }

        boolean esCero() {
            return n.signum() == 0;
        }

        @Override
        public String toString() {
            if (d.equals(BigInteger.ONE)) {
                return n.toString();
            }

            return n + "/" + d;
        }
    }

    // =====================================================
    // ENTRADA DE DATOS CON JOPTIONPANE
    // =====================================================

    static Fraccion pedirNumero(String mensaje) {
        while (true) {
            String texto = JOptionPane.showInputDialog(
                null,
                mensaje
                    + "\n\nPuedes ingresar enteros, decimales "
                    + "o fracciones.\nEjemplos: 5, -2.5, 3/4",
                "Ingreso de datos",
                JOptionPane.QUESTION_MESSAGE
            );

            if (texto == null) {
                throw new CancelarEntrada();
            }

            try {
                return Fraccion.leer(texto);
            } catch (NumberFormatException | ArithmeticException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "El dato ingresado no es valido.\n"
                        + "Ejemplos: 5, -2.5 o 3/4.",
                    "Dato incorrecto",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    static int pedirEntero(String mensaje) {
        while (true) {
            String texto = JOptionPane.showInputDialog(
                null,
                mensaje,
                "Ingreso de datos",
                JOptionPane.QUESTION_MESSAGE
            );

            if (texto == null) {
                throw new CancelarEntrada();
            }

            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Ingresa un numero entero valido.",
                    "Dato incorrecto",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    // =====================================================
    // MOSTRAR RESULTADOS EN UNA VENTANA
    // =====================================================

    static void mostrarResultados(String titulo, String contenido) {
        JTextArea area = new JTextArea(contenido, 25, 65);

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setCaretPosition(0);

        JScrollPane desplazamiento = new JScrollPane(area);

        JOptionPane.showMessageDialog(
            null,
            desplazamiento,
            titulo,
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Captura la salida de los calculos para mostrarla en una ventana.
    static void ejecutarYMostrar(
        String titulo,
        Runnable operacion
    ) {
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream salidaTemporal = new PrintStream(
            buffer,
            true,
            StandardCharsets.UTF_8
        )) {
            System.setOut(salidaTemporal);

            operacion.run();

            salidaTemporal.flush();

        } catch (CancelarEntrada e) {
            // El usuario cancelo un ingreso de datos.
            return;

        } catch (ArithmeticException e) {
            System.setOut(salidaOriginal);

            JOptionPane.showMessageDialog(
                null,
                "No se pudo completar el calculo:\n" + e.getMessage(),
                "Error matematico",
                JOptionPane.ERROR_MESSAGE
            );

            return;

        } catch (RuntimeException e) {
            System.setOut(salidaOriginal);

            JOptionPane.showMessageDialog(
                null,
                "Ocurrio un error inesperado:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );

            return;

        } finally {
            System.setOut(salidaOriginal);
        }

        String resultado = buffer.toString(StandardCharsets.UTF_8);

        if (!resultado.isBlank()) {
            mostrarResultados(titulo, resultado);
        }
    }

    static void mostrarMatriz(
        String titulo,
        Fraccion[][] matriz
    ) {
        System.out.println("\n" + titulo);

        for (Fraccion[] fila : matriz) {
            System.out.print("[ ");

            for (Fraccion valor : fila) {
                System.out.printf("%10s ", valor);
            }

            System.out.println("]");
        }
    }

    // =====================================================
    // OPCION 1: FACTORIZACION LU
    // =====================================================

    static void factorizacionLU() {
        Fraccion[][] a = new Fraccion[3][3];
        Fraccion[][] l = new Fraccion[3][3];
        Fraccion[][] u = new Fraccion[3][3];

        System.out.println("FACTORIZACION LU DE UNA MATRIZ 3x3");
        System.out.println(
            "Ingrese los nueve elementos de la matriz."
        );

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                a[i][j] = pedirNumero(
                    "Ingrese el elemento a" + (i + 1) + (j + 1)
                );

                u[i][j] = a[i][j];

                l[i][j] = new Fraccion(i == j ? 1 : 0);
            }
        }

        mostrarMatriz("MATRIZ ORIGINAL A:", a);

        for (int pivote = 0; pivote < 2; pivote++) {
            if (u[pivote][pivote].esCero()) {
                System.out.println("\nNo se puede continuar.");
                System.out.println(
                    "El pivote es cero y esta version no "
                    + "implementa pivoteo."
                );
                System.out.println(
                    "Prueba con una matriz que permita "
                    + "la factorizacion LU sin intercambiar filas."
                );
                return;
            }

            for (int fila = pivote + 1; fila < 3; fila++) {
                Fraccion multiplicador =
                    u[fila][pivote].dividir(u[pivote][pivote]);

                l[fila][pivote] = multiplicador;

                System.out.println(
                    "\nF" + (fila + 1) + " = F" + (fila + 1)
                    + " - (" + multiplicador + ") * F"
                    + (pivote + 1)
                );

                for (int columna = 0; columna < 3; columna++) {
                    Fraccion anterior = u[fila][columna];
                    Fraccion referencia = u[pivote][columna];

                    u[fila][columna] = anterior.restar(
                        multiplicador.multiplicar(referencia)
                    );

                    System.out.println(
                        "Columna " + (columna + 1) + ": "
                        + anterior + " - (" + multiplicador + ") * ("
                        + referencia + ") = " + u[fila][columna]
                    );
                }

                mostrarMatriz(
                    "Matriz U despues de la operacion:",
                    u
                );
            }
        }

        mostrarMatriz("MATRIZ L:", l);
        mostrarMatriz("MATRIZ U:", u);

        Fraccion[][] resultado = new Fraccion[3][3];
        boolean coincide = true;

        System.out.println("\nCOMPROBACION L * U = A");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Fraccion suma = new Fraccion(0);

                System.out.print("c" + (i + 1) + (j + 1) + " = ");

                for (int k = 0; k < 3; k++) {
                    if (k > 0) {
                        System.out.print(" + ");
                    }

                    System.out.print(
                        "(" + l[i][k] + ") * (" + u[k][j] + ")"
                    );

                    suma = suma.sumar(
                        l[i][k].multiplicar(u[k][j])
                    );
                }

                resultado[i][j] = suma;

                System.out.println(" = " + suma);

                if (!suma.restar(a[i][j]).esCero()) {
                    coincide = false;
                }
            }
        }

        mostrarMatriz("RESULTADO DE L * U:", resultado);

        if (coincide) {
            System.out.println(
                "\nCOMPROBACION CORRECTA: L * U = A."
            );
        } else {
            System.out.println(
                "\nL * U no coincide con la matriz original."
            );
        }
    }

    // =====================================================
    // OPCION 2: METODO DE GRAM-SCHMIDT
    // =====================================================

    static Fraccion productoPunto(
        Fraccion[] a,
        Fraccion[] b
    ) {
        Fraccion suma = new Fraccion(0);

        for (int i = 0; i < a.length; i++) {
            suma = suma.sumar(
                a[i].multiplicar(b[i])
            );
        }

        return suma;
    }

    static Fraccion[] pedirVector(int n, int numero) {
        Fraccion[] vector = new Fraccion[n];

        System.out.println("\nVECTOR " + numero);

        for (int i = 0; i < n; i++) {
            vector[i] = pedirNumero(
                "Vector " + numero
                + ": ingresa la componente " + (i + 1)
            );
        }

        return vector;
    }

    static void mostrarVector(
        String nombre,
        Fraccion[] vector
    ) {
        System.out.print(nombre + " = (");

        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }

            System.out.print(vector[i]);
        }

        System.out.println(")");
    }

    static void gramSchmidt() {
        System.out.println("METODO DE GRAM-SCHMIDT");

        int n = pedirEntero(
            "Ingresa la dimension de los vectores (minimo 3):"
        );

        if (n < 3) {
            System.out.println(
                "La dimension debe ser al menos 3."
            );
            return;
        }

        if (n > 100) {
            System.out.println(
                "Para esta version, utiliza una dimension "
                + "de 100 o menos."
            );
            return;
        }

        Fraccion[] v1 = pedirVector(n, 1);
        Fraccion[] v2 = pedirVector(n, 2);
        Fraccion[] v3 = pedirVector(n, 3);

        Fraccion[] u1 = v1.clone();

        Fraccion p11 = productoPunto(u1, u1);

        if (p11.esCero()) {
            System.out.println(
                "El primer vector no puede ser cero."
            );
            return;
        }

        Fraccion coef21 =
            productoPunto(v2, u1).dividir(p11);

        Fraccion[] u2 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            u2[i] = v2[i].restar(
                coef21.multiplicar(u1[i])
            );
        }

        Fraccion p22 = productoPunto(u2, u2);

        if (p22.esCero()) {
            System.out.println(
                "El segundo vector depende del primero."
            );

            System.out.println(
                "No se pueden obtener tres vectores "
                + "ortogonales no nulos con este conjunto."
            );

            return;
        }

        Fraccion coef31 =
            productoPunto(v3, u1).dividir(p11);

        Fraccion coef32 =
            productoPunto(v3, u2).dividir(p22);

        Fraccion[] u3 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            u3[i] = v3[i]
                .restar(coef31.multiplicar(u1[i]))
                .restar(coef32.multiplicar(u2[i]));
        }

        System.out.println("\nRESULTADOS:");

        mostrarVector("u1", u1);
        mostrarVector("u2", u2);
        mostrarVector("u3", u3);

        Fraccion p12 = productoPunto(u1, u2);
        Fraccion p13 = productoPunto(u1, u3);
        Fraccion p23 = productoPunto(u2, u3);

        System.out.println("\nCOMPROBACION DE ORTOGONALIDAD:");

        System.out.println("u1 . u2 = " + p12);
        System.out.println("u1 . u3 = " + p13);
        System.out.println("u2 . u3 = " + p23);

        if (productoPunto(u3, u3).esCero()) {
            System.out.println(
                "\nEl tercer vector es una combinacion "
                + "lineal de los anteriores."
            );

            System.out.println(
                "El conjunto original es linealmente dependiente."
            );

        } else if (
            p12.esCero()
            && p13.esCero()
            && p23.esCero()
        ) {
            System.out.println(
                "\nCOMPROBACION CORRECTA: "
                + "los tres vectores son ortogonales."
            );

        } else {
            System.out.println(
                "\nLa comprobacion de ortogonalidad fallo."
            );
        }
    }

    // =====================================================
    // OPCION 3: TRANSFORMACIONES LINEALES
    // =====================================================

    static void transformaciones() {
        int[][][] matrices = {
            {{1, 2}, {2, -1}, {1, 3}},
            {{2, -1}, {1, 3}, {4, -1}},
            {{3, 1}, {1, -2}, {2, 4}},
            {{1, -3}, {2, 2}, {5, -1}}
        };

        String[] formulas = {
            "T(x,y) = (x + 2y, 2x - y, x + 3y)",
            "T(x,y) = (2x - y, x + 3y, 4x - y)",
            "T(x,y) = (3x + y, x - 2y, 2x + 4y)",
            "T(x,y) = (x - 3y, 2x + 2y, 5x - y)"
        };

        for (int p = 0; p < matrices.length; p++) {
            int[][] m = matrices[p];

            System.out.println(
                "\n================================"
            );

            System.out.println("PROBLEMA " + (p + 1));
            System.out.println(formulas[p]);

            System.out.println(
                "T(1,0) = ("
                + m[0][0] + ", "
                + m[1][0] + ", "
                + m[2][0] + ")"
            );

            System.out.println(
                "T(0,1) = ("
                + m[0][1] + ", "
                + m[1][1] + ", "
                + m[2][1] + ")"
            );

            System.out.println("MATRIZ ASOCIADA:");

            for (int[] fila : m) {
                System.out.println(
                    "[ " + fila[0] + "    " + fila[1] + " ]"
                );
            }
        }

        System.out.println(
            "\nSe mostraron las cuatro transformaciones."
        );
    }

    // =====================================================
    // MENU PRINCIPAL
    // =====================================================

    static void mostrarBienvenida() {
        JOptionPane.showMessageDialog(
            null,
            "Bienvenido al programa de Algebra Lineal.\n\n"
                + "Aqui puedes trabajar con:\n"
                + "• Factorizacion LU\n"
                + "• Metodo de Gram-Schmidt\n"
                + "• Transformaciones lineales\n\n"
                + "Los resultados se mostraran en ventanas.",
            "Algebra Lineal",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {

        // Intenta utilizar el estilo visual del sistema operativo.
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (
            UnsupportedLookAndFeelException
            | ClassNotFoundException
            | InstantiationException
            | IllegalAccessException e
        ) {
            // Si falla, Java utiliza su estilo predeterminado.
        }

        mostrarBienvenida();

        String[] opciones = {
            "Factorizacion LU",
            "Metodo de Gram-Schmidt",
            "Transformaciones lineales",
            "Salir"
        };

        boolean continuar = true;

        while (continuar) {
            int opcion = JOptionPane.showOptionDialog(
                null,
                "Selecciona el metodo que deseas ejecutar:",
                "MENU PRINCIPAL - ALGEBRA LINEAL",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
            );

            switch (opcion) {
                case 0:
                    ejecutarYMostrar(
                        "Resultados - Factorizacion LU",
                        AlgebraLineal::factorizacionLU
                    );
                    break;

                case 1:
                    ejecutarYMostrar(
                        "Resultados - Gram-Schmidt",
                        AlgebraLineal::gramSchmidt
                    );
                    break;

                case 2:
                    ejecutarYMostrar(
                        "Resultados - Transformaciones lineales",
                        AlgebraLineal::transformaciones
                    );
                    break;

                case 3:
                case JOptionPane.CLOSED_OPTION:
                    continuar = false;
                    break;

                default:
                    continuar = false;
                    break;
            }
        }

        JOptionPane.showMessageDialog(
            null,
            "Gracias por utilizar el programa.\n"
                + "Programa finalizado correctamente.",
            "Hasta pronto",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}