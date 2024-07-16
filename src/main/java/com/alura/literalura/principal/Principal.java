package com.alura.literalura.principal;


import com.alura.literalura.model.*;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoApi;
import com.alura.literalura.service.ConvierteDatos;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoApi consumoAPI = new ConsumoApi();
    private ConvierteDatos convierteDatos = new ConvierteDatos();
    private LibroRepository repositorio;

    public Principal(LibroRepository repository) {
        this.repositorio = repository;
    }

    public void mostrarMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    ************************************************
                    1 - Buscar Libros por Titulo
                    2 - Libros Buscados
                    3 - Autores buscados
                    4 - Buscar autores vivos por año
                    5 - Buscar Libros por idioma
                    6 - Top 10 Libros mas descargados
                    7 - Estadísticas

                    0 - Salir
                    ************************************************
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    mostrarLibrosBuscados();
                    break;
                case 3:
                    mostrarAutoresBuscados();
                    break;
                case 4:
                    buscarAutoresVivosPorAño();
                    break;
                case 5:
                    buscarLibrosPorIdioma();
                    break;
                case 6:
                    top10Libros();
                    break;
                case 7:
                    estadisticasGenerales();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }



    private void estadisticasGenerales() {
        List<Libro> listaDeLibros = repositorio.findAll();
        DoubleSummaryStatistics est = listaDeLibros.stream().collect(Collectors.summarizingDouble(Libro::getNumeroDeDescargas));
        System.out.println("************************ ESTADÍSTICAS ************************\n");
        System.out.println("Cantidad de libros: " + est.getCount());
        System.out.println("Promedio de descargas: " + est.getAverage());
        System.out.println("Mínimo de descargas: " + est.getMin());
        System.out.println("Maximo de descargas: " + est.getMax() + "\n");

    }

    private void top10Libros() {

        List<Libro> listaTop10Libros = repositorio.findTop10ByOrderByNumeroDeDescargasDesc();
        System.out.println("************************ TOP 10 LIBROS ************************\n");
        listaTop10Libros.forEach(l -> System.out.println(
                "Libro: " + l.getTitulo() + " | Descargas: " + l.getNumeroDeDescargas()));
        System.out.println();

    }

    private void buscarLibrosPorIdioma() {

        System.out.println("""
                Seleccione el idioma en el que desea buscar el libro:
                ES: Español
                EN: Ingles
                FR: Frances
                IT: Italiano
                PT: Portugues
                """);

        var idiomaSelecionado = teclado.nextLine();

        try {

            List<Libro> libroPorIdioma = repositorio.findByIdiomas(Idioma.valueOf(idiomaSelecionado.toUpperCase()));
            libroPorIdioma.forEach(n -> System.out.println(
                    "************************ LIBRO ************************\n" +
                    "\nTitulo: " + n.getTitulo() +
                    "\nIndioma: " + n.getIdiomas() +
                    "\nAutor: " + n.getAutor().stream().map(Autor::getNombre).collect(Collectors.joining()) +
                    "\nNumero de descargas: " + n.getNumeroDeDescargas() +
                    "\n"
            ));
        } catch (IllegalArgumentException e){

            System.out.println("Idioma no existe...\n");
        }

    }

    private void buscarAutoresVivosPorAño() {

        System.out.println("Ingresa el año a consultar:");
        String año = teclado.nextLine();

        List<Autor> autoresVivos = repositorio.mostrarAutoresVivos(año);

        if (autoresVivos.isEmpty()){
            System.out.println("Sin autores vivos en el año indicado...\n");
            return;
        }

        Map<String, List<String>> autoresConLibros = autoresVivos.stream()
                .collect(Collectors.groupingBy(
                        Autor::getNombre,
                        Collectors.mapping(a -> a.getLibro().getTitulo(), Collectors.toList())
                ));

        autoresConLibros.forEach((nombre, libros) -> {
            Autor autor = autoresVivos.stream()
                    .filter(a -> a.getNombre().equals(nombre))
                    .findFirst().orElse(null);
            if (autor != null) {
                System.out.println(
                        "************************ AUTOR ************************\n" +
                        "\nNombre: " + nombre +
                        "\nFecha de nacimiento: " + autor.getFechaDeNacimiento() +
                        "\nFecha de defunción: " + autor.getFechaDeDefuncion() +
                        "\nLibros: " + libros + "\n");
            }
        });
    }

    private void mostrarAutoresBuscados() {
        List<Autor> autoresBuscados = repositorio.mostrarAutores();

        Map<String, List<String>> autoresConLibros = autoresBuscados.stream()
                .collect(Collectors.groupingBy(
                        Autor::getNombre,
                        Collectors.mapping(a -> a.getLibro().getTitulo(), Collectors.toList())
                ));

        autoresConLibros.forEach((nombre, libros) -> {
            Autor autor = autoresBuscados.stream()
                    .filter(a -> a.getNombre().equals(nombre))
                    .findFirst().orElse(null);
            if (autor != null) {
                System.out.println(
                        "************************ AUTOR ************************\n" +
                        "\nNombre: " + nombre +
                        "\nFecha de nacimiento: " + autor.getFechaDeNacimiento() +
                        "\nFecha de defunción: " + autor.getFechaDeDefuncion() +
                        "\nLibros: " + libros + "\n");
            }
        });

    }

    private void mostrarLibrosBuscados() {
        List<Libro> mostrarListaLibros = repositorio.findAll();
        mostrarListaLibros.forEach(l -> System.out.println(
                "************************ LIBRO ************************\n" +
                "\nTítulo: " + l.getTitulo() +
                "\nIdioma: " + l.getIdiomas() +
                "\nAutor: " + l.getAutor().stream().map(Autor::getNombre).collect(Collectors.joining()) +
                "\nNúmero de descargas: " + l.getNumeroDeDescargas() +
                "\n"
        ));
    }

    //Primero busco el libro con la API
    private DatosLibro buscarLibroAPI(){
        System.out.println("Escriba el nombre del Libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        var json = ConsumoApi.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = convierteDatos.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

        if (libroBuscado.isPresent()){
            System.out.println("Libro encontrado!");
            return libroBuscado.get();
        } else {
            System.out.println("Libro no encontrado, intente nuevamente\n");
            return null;
        }
    }
    private void buscarLibroPorTitulo() {
        Optional<DatosLibro> datosOpcional = Optional.ofNullable(buscarLibroAPI());

        if(datosOpcional.isPresent()) {
            DatosLibro datos = datosOpcional.get();

            Libro libro = new Libro(datos);
            List<Autor> autores = new ArrayList<>();
            for (DatosAutor datosAutor : datos.autor()) {
                Autor autor = new Autor(datosAutor);
                autor.setLibro(libro);
                autores.add(autor);
            }
            libro.setAutor(autores);
            try {
                repositorio.save(libro);
                System.out.println(libro.getTitulo() + " guardado exitosamente!!!");
            } catch (DataIntegrityViolationException e) {
                System.out.println("Error: libro ya está almacenado en la base de datos, intenta con otro libro.\n");
            }
        }
    }
}