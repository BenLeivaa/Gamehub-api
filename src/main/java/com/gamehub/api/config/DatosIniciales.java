package com.gamehub.api.config;

import com.gamehub.api.model.Categoria;
import com.gamehub.api.model.Producto;
import com.gamehub.api.model.Rol;
import com.gamehub.api.model.Usuario;
import com.gamehub.api.repository.CategoriaRepository;
import com.gamehub.api.repository.ProductoRepository;
import com.gamehub.api.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Al arrancar, si las tablas están vacías, carga datos de prueba.
 * Cambia los productos por los de tu tienda si quieres.
 */
@Configuration
@Slf4j
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarDatos(UsuarioRepository usuarios, CategoriaRepository categorias,
                                  ProductoRepository productos, PasswordEncoder encoder) {
        return args -> {
            if (usuarios.count() == 0) {
                usuarios.save(usuario("Administrador GameHub", "admin@gamehub.cl", "Admin1234", Rol.ADMINISTRADOR, encoder));
                usuarios.save(usuario("Vendedor GameHub", "vendedor@gamehub.cl", "Vendedor1234", Rol.VENDEDOR, encoder));
                usuarios.save(usuario("Cliente de Prueba", "cliente@gamehub.cl", "Cliente1234", Rol.CLIENTE, encoder));
                log.info("Usuarios de prueba creados (admin, vendedor, cliente)");
            }

            if (categorias.count() == 0) {
                Categoria consolas = categorias.save(categoria("Consolas", "Consolas de videojuegos"));
                Categoria juegos = categorias.save(categoria("Juegos", "Videojuegos para todas las plataformas"));
                Categoria perifericos = categorias.save(categoria("Periféricos", "Teclados, mouse y audífonos gamer"));
                Categoria accesorios = categorias.save(categoria("Accesorios", "Controles y accesorios"));

                if (productos.count() == 0) {
                    productos.save(producto("PlayStation 5", "Sony", "Slim", 549990.0, 10, consolas));
                    productos.save(producto("Xbox Series X", "Microsoft", "1TB", 529990.0, 8, consolas));
                    productos.save(producto("Nintendo Switch OLED", "Nintendo", "OLED", 349990.0, 12, consolas));
                    productos.save(producto("The Legend of Zelda: TOTK", "Nintendo", "Switch", 59990.0, 25, juegos));
                    productos.save(producto("EA Sports FC 25", "EA", "PS5", 49990.0, 30, juegos));
                    productos.save(producto("Teclado Mecánico RGB", "Redragon", "Kumara K552", 39990.0, 20, perifericos));
                    productos.save(producto("Mouse Gamer", "Logitech", "G203", 24990.0, 40, perifericos));
                    productos.save(producto("Control DualSense", "Sony", "PS5", 69990.0, 15, accesorios));
                }
                log.info("Categorías y productos de prueba creados");
            }
        };
    }

    private Usuario usuario(String nombre, String email, String clave, Rol rol, PasswordEncoder encoder) {
        return Usuario.builder()
                .nombre(nombre).email(email)
                .passwordHash(encoder.encode(clave))
                .telefono("+56900000000")
                .rol(rol).estado(true)
                .build();
    }

    private Categoria categoria(String nombre, String descripcion) {
        return Categoria.builder().nombre(nombre).descripcion(descripcion).estado(true).build();
    }

    private Producto producto(String nombre, String marca, String modelo, double precio, int stock, Categoria categoria) {
        return Producto.builder()
                .nombre(nombre).marca(marca).modelo(modelo)
                .precio(precio).stock(stock)
                .descripcion(nombre + " - " + marca)
                .estado(true).categoria(categoria)
                .build();
    }
}
