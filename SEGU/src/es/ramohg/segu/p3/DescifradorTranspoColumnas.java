package es.ramohg.segu.p3;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Descifrador de textos cifrados por transposición de columnas por fuerza bruta
 * @author Ramón Hernández García
 * @version 1.1.0
 *
 */
public class DescifradorTranspoColumnas {
	private String textoCifrado;
	private int longitudClave;
	private String pista;
	
	/**
	 * Crea una instancia del descifrador
	 * @param textoCifrado es el texto que se debe descifrar
	 * @param longitudClave es la longitud de la clave que se intenta forzar
	 * @param pista es una palabra que conocemos del texto descifrado
	 */
	public DescifradorTranspoColumnas(String textoCifrado, int longitudClave, String pista) {
		this.textoCifrado = textoCifrado;
		this.longitudClave = longitudClave;
		this.pista = pista;
	}

	/**
	 * Calcula el factorial de un número recursivamente
	 * @param numero número del que se calcula el factorial
	 * @return el factorial del número pasado como parámetro
	 */
	private int factorial(int numero) {
		if (numero <= 1)
			return 1;
		return numero*factorial(numero-1);
	}

	/**
	 * Obtiene las posibles combinaciones de espacios para matrices irregulares
	 * @return un array combinaciones de espacios por columnas
	 */
	private int[][] getCombinacionesEspacios() {
		int filas = textoCifrado.length()/longitudClave, columnas = this.longitudClave, nEspacios, numeroCombinaciones;
		filas = ((textoCifrado.length() % longitudClave) > 0) ? filas+1 : filas;
		nEspacios = filas*columnas - textoCifrado.length();
		numeroCombinaciones = factorial(longitudClave)/(factorial(nEspacios) * factorial(longitudClave - nEspacios));
		int [][] combEsps = new int[numeroCombinaciones][nEspacios];
			//Ignorar lo de arriba
			HashSet<Nodo> nodosVisitados = new HashSet<Nodo>();
			Nodo nodoPrincipal = new Nodo();
			Nodo nodoActual = nodoPrincipal;
			int indiceVisitados = 1;
			buclePpal: while (indiceVisitados < numeroCombinaciones) {
				//Creo los subnodos si el nodo destino tiene nivel menor o igual que el nivel máximo
				if ((nodoActual.nivel + 1) <= nEspacios ) {
					//Creo los subnodos solo si no están creados de antes
					if (nodoActual.subNodos.isEmpty()) {
						for (int i = 1; i <= longitudClave; i++) {
							//Creo un nuevo subNodo si tiene indice mayor al que tenemos ahora
							if (i > nodoActual.i) {
								Nodo nuevoSubNodo = new Nodo(i, nodoActual.nivel+1, nodoActual);
								nuevoSubNodo.nodoAnterior = nodoActual;
								nodoActual.subNodos.add(nuevoSubNodo);
							}
						}
					}
					for (Nodo subNodo: nodoActual.subNodos) {
						// Busco un nodo no visitado
						if (!nodosVisitados.contains(subNodo)/*!nodoVisitado(nodosVisitados, subNodo)*/) {
							nodoActual = subNodo;
							continue buclePpal;
						}
					}
					//Todos mis nodos están visitados, marco este nodo como visitado y me vuelvo al anterior si existe
					nodosVisitados.add(nodoActual);//addNodoVisitado(nodosVisitados, nodoActual);
					if (nodoActual.nodoAnterior == null) {
						//Si no hay nodo anterior acabamos
						break buclePpal;
					} else {
						//Si hay nodo anterior se vuelve a analizar
						nodoActual = nodoActual.nodoAnterior;
					}
				} else {
					//Marco el nodo como visitado y saco y guardo su combinación
					nodosVisitados.add(nodoActual);//addNodoVisitado(nodosVisitados, nodoActual);
					guardaNodoEnCombinaciones(combEsps, nodoActual);
					nodoActual = nodoActual.nodoAnterior;
				}
			}
			/**
			 * Nodo 0 id -1
					creo nodos 1 2 3 4 5 (con i mayor que la i que ya tenemos)
					Me meto en el primero no visitado de los creados si el nivel es menor o igual que nEspacios.
					 ->Si lo encuentro, me meto en él y repito el creo nodos con i mayor que la i que ya tenemos
					 ->Si no encuentro cojo el nodo anterior (marcando el nodo como visitado) si no es null y busco el primero no visitado.
					 ->Si no encuentro y el nodo anterior es null, (marco como visitado este nodo) y termino
					Si el nivel es mayor que nEspacios, (se marca el nodo como visitado), se llama a guardaNodo(combEsps, nodo), y se vuelve al anterior, buscando un nodo no visitado

			 * */
		return combEsps;
	}
	
	/**
	 * Obtiene todas las posibles combinaciones de columnas para descifrar el texto
	 * @return un array de combinaciones de las columnas
	 */
	private int[][] getCombinacionesColumnas() {
		int numeroCombinaciones = factorial(longitudClave);
		int [][] combCols = new int[numeroCombinaciones][longitudClave];
		for (int posCifraActual = 0; posCifraActual < longitudClave; posCifraActual++) {
			int combActual = 0;
			while (combActual < numeroCombinaciones) {
				ArrayList<Integer> numerosYaMetidos = new ArrayList<Integer>();
				//Añado los números anteriores de cada combinacion
				for (int i = 0; i < posCifraActual; i++) {
					numerosYaMetidos.add(combCols[combActual][i]);
				}
				//Recojo y recorro los números siguientes
				for (int numAMeter: getSiguienteNumeroSinRepeticionImportaOrden(numerosYaMetidos)) {
					//Se copia el número (longitudClave -(posCifraActual+1))! veces
					for (int i = 0; i < factorial(longitudClave -(posCifraActual+1)); i++) {
						combCols[combActual][posCifraActual] = numAMeter;
						combActual++;
					}
				}
			}
		}
		return combCols;
	}

	/**
	 * Extrae y guarda la combinación de espacios a partir de las combinaciones de espacios
	 * ya existentes y del nodo terminal, del que además se extraen los nodos anteriores
	 * @param combEsps array de combinaciones de espacios donde se guardará la combinación hallada
	 * @param nodo nodo terminal del que se saca una combinación de espacios
	 */
	private void guardaNodoEnCombinaciones(int[][] combEsps, Nodo nodo) {
		int longitud = combEsps.length, longitudCombinaciones = combEsps[0].length;
		for (int i = 0; i < longitud; i++) {
			boolean vacio = true;
			for (int j = 0; j < longitudCombinaciones; j++) {
				if (combEsps[i][j] != 0) {
					vacio = false;
					break;
				}
			}
			if (vacio) {
				Nodo nodoVisitado = nodo;
				for (int j = longitudCombinaciones - 1; j >= 0; j--) {
					if (nodoVisitado == null) {
						System.out.println("¡Cuidado! Nodo nulo en guardaNodoEnCombinaciones");
						return;
					}
					combEsps[i][j] = nodoVisitado.i;
					nodoVisitado = nodoVisitado.nodoAnterior;
				}
				return;
			}
		}
		System.out.println("No se ha guardado la combinacion del nodo en guardaNodoEnCombinaciones");
	}

	/**
	 * Obtiene los siguientes posibles números de una combinación de columnas a partir de las anteriores
	 * @param numerosYaMetidos los números de las columnas que ya se han metido en posiciones anteriores de la combinación
	 * @return la lista de posibles nuevos números para una posición de una combinación de columnas
	 */
	private ArrayList<Integer> getSiguienteNumeroSinRepeticionImportaOrden(ArrayList<Integer> numerosYaMetidos) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i = 1; i <= longitudClave; i++) {
			if (!numerosYaMetidos.contains(i)) {
				res.add(i);
			}
		}
		return res;
	}

	/**
	 * Dibuja una matriz pasada como parámetro imprimiendo fila a fila
	 * @param matriz es la matriz que se dibuja
	 */
	public void dibujarMatriz(char[][] matriz) {
		if (matriz == null || matriz.length == 0 || matriz[0].length == 0) {
			return;
		}
		int filas = matriz.length, columnas = matriz[0].length;
		for (int fila = 0; fila < filas; fila++) {
			String strFila = "";
			for (int columna = 0; columna < columnas; columna++) {
				strFila = strFila.concat("\t["+matriz[fila][columna]+"]");
			}
			System.out.println(strFila);
		}
	}

	/**
	 * Nos informa de si una matriz de descifrado contiene entre sus filas la pista
	 * @param matriz es la matriz en la que queremos saber si se encuentra la pista
	 * @return boolean que nos dice si contiene la pista o no
	 */
	private boolean matrizContienePista(char[][] matriz) {
		return matrizAString(matriz).contains(this.pista.toLowerCase());
	}

	/**
	 * Pasa una matriz a una string juntando todas sus filas en una única línea
	 * @param matriz es la matriz de la que se quiere obtener una string
	 * @return string con el contenido de la matriz
	 */
	public String matrizAString(char[][] matriz) {
		int filas = matriz.length, columnas = matriz[0].length;
		String str = "";
		for (int i = 0; i < filas; i++) {
			for (int j = 0; j < columnas; j++) {
				str = str.concat(""+Character.toLowerCase(matriz[i][j]));
			}
		}
		return str;
	}

	/**
	 * Reordena las columnas de una matriz a partir de una combinación de columnas dada
	 * @param ordenColumnas orden de las columnas de la nueva matriz
	 * @param matriz es la matriz que se desea reordenar
	 * @return matriz con columnas reordenadas
	 */
	public char[][] reordenarMatriz(int[] ordenColumnas, char[][] matriz) {
		char[][] res = new char[matriz.length][matriz[0].length];
		int columnaSrc = 0;
		for (int columnaDst: ordenColumnas) {
			for (int fila = 0; fila < matriz.length; fila++) {
				res[fila][columnaDst - 1] = matriz[fila][columnaSrc];
			}
			columnaSrc++;
		}
		return res;
	}

	/**
	 *  
	 */
	public void descifrarTexto() {
		for (int[] posEspacios: getCombinacionesEspacios()) {
			for (int[] combCols: getCombinacionesColumnas()) {
				char[][] matriz = reordenarMatriz(combCols, obtenerMatrizCodificada(posEspacios));
				if (matrizContienePista(matriz)) { //No encuentra ninguna combinacion
					dibujarMatriz(matriz);
					System.out.println("Espacios en: "+imprimeArray(posEspacios));
					System.out.println("Orden de las columnas: "+imprimeArray(combCols));
					System.out.println("Texto descifrado: "+matrizAString(matriz));
				}
			}
		}
	}

	/**
	 * Obtiene la matriz con el texto cifrado añadiendo espacios en las columnas especificadas en la última fila de la matriz
	 * @param posicionesEspacios array con las columnas que contienen un espacio en la última fila
	 * @return matriz formada con el texto y los espacios
	 */
	public char[][] obtenerMatrizCodificada(int[] posicionesEspacios) {
		int filas = textoCifrado.length()/longitudClave, columnas = this.longitudClave;
		filas = ((textoCifrado.length() % longitudClave) > 0) ? filas+1 : filas;
		char[][] matriz = new char[filas][columnas];
		int idxStr = 0;
		for (int columna = 0; columna < columnas; columna++) {
			for (int fila = 0; fila < filas; fila++) {
				boolean espacio = false;
				if (fila == filas-1) {
					for (int posEspacio: posicionesEspacios) {
						if (columna == (posEspacio - 1)) {
							espacio = true;
							break;
						}
					}
				}
				if (espacio) {
					matriz[fila][columna] = '_';
				} else {
					matriz[fila][columna] = textoCifrado.charAt(idxStr);
					idxStr++;
				}
			}
		}
		return matriz;
	}

	/**
	 * Imprime un array especificado como parrámetro
	 * @param array es el array que se imprime por la consola
	 */
	private String imprimeArray(int[] array) {
		String strComb = "{";
		for (int numero: array) {
			if (numero == array[array.length-1]) {
				strComb = strComb.concat(""+numero);
			} else {
				strComb = strComb.concat(""+numero+", ");
			}
		}
		return strComb+"}";
	}

/**
 * Clase de nodos para formar árboles para el cálculo de las combinaciones de espacios que se pueden hacer
 * @author Ramón Hernández García
 *
 */
class Nodo {
	int i, nivel; //i es el número asociado al nodo, nivel es el árbol en el que se encuentra (1º, 2º, 3º...)
	public Nodo nodoAnterior;
	public ArrayList<Nodo> subNodos;
	Nodo() {
		this.i = -1;
		this.nivel = 0;
		this.subNodos = new ArrayList<Nodo>();
		this.nodoAnterior = null;
	}
	Nodo(int i, int nivel) {
		this.i = i;
		this.nivel = nivel;
		this.subNodos = new ArrayList<Nodo>();
		this.nodoAnterior = null;
	}
	Nodo(int i, int nivel, Nodo nodoAnterior) {
		this.i = i;
		this.nivel = nivel;
		this.subNodos = new ArrayList<Nodo>();
		this.nodoAnterior = nodoAnterior;
	}
}
