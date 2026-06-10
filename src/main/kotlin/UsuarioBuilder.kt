/**
 * Ejercicio 5: It y Scope Functions (run, apply, also, let)
 *
 * Implementa los métodos de esta clase para que pasen todos los tests
 * del archivo Ejercicio5ItScopeFunctionsTest.kt
 *
 * IMPORTANTE: No modifiques la firma de los métodos, solo implementa su lógica.
 * IMPORTANTE: Debes usar las scope functions indicadas en cada sección.
 */

data class Usuario(
    var id: Int = 0,
    var nombre: String = "",
    var email: String = "",
    var activo: Boolean = false,
    var roles: MutableList<String> = mutableListOf(),
    var configuracion: ConfiguracionUsuario = ConfiguracionUsuario(),
)

data class ConfiguracionUsuario(
    var tema: String = "claro",
    var idioma: String = "es",
    var notificaciones: Boolean = true,
    var nivelPrivacidad: Int = 1,
)

data class Validacion(
    val campo: String,
    val valido: Boolean,
    val mensaje: String,
)

class UsuarioBuilder {
    // Parte A: Uso del parámetro implícito 'it'

    fun procesarNumeros(numeros: List<Int>): List<Int> {
        return numeros.filter { it % 2 == 0 }.map { it * 10 }
    }

    fun validarUsuarios(usuarios: List<Usuario>): List<List<Validacion>> {
        return usuarios.map {
            listOf(
                Validacion("nombre", it.nombre.isNotEmpty(), "Nombre vacío"),
                Validacion("email", it.email.contains("@"), "Email inválido"),
                Validacion("roles", it.roles.isNotEmpty(), "Sin roles")
            )
        }
    }

    fun procesarTextos(textos: List<String>): List<String> {
        return textos
            .map { it.trim() }
            .map { it.lowercase() }
            .filter { it.isNotEmpty() }
    }

    // Parte B: Función run

    fun calcularNivelAcceso(usuario: Usuario): Int {
        return usuario.run {
            val puntosActivo = if (activo) 10 else 0
            val puntosRoles = roles.size * 5
            val puntosEmail = if (email.contains("@empresa.com") || email.contains("@corp.com")) 5 else 0
            puntosActivo + puntosRoles + puntosEmail
        }
    }

    fun crearUsuarioConTipo(tipo: String): Usuario {
        return Usuario().run {
            when (tipo.uppercase()) {
                "ADMIN" -> {
                    roles = mutableListOf("ADMIN")
                    configuracion.nivelPrivacidad = 3
                    configuracion.notificaciones = true
                }
                "USER" -> {
                    roles = mutableListOf("USER")
                    configuracion.nivelPrivacidad = 1
                    configuracion.notificaciones = false
                }
            }
            this
        }
    }

    // Parte C: Función apply

    fun crearUsuarioCompleto(
        nombre: String,
        email: String,
        roles: List<String>,
    ): Usuario {
        return Usuario().apply {
            this.nombre = nombre
            this.email = email
            this.activo = true
            this.roles = roles.toMutableList()
            this.configuracion = ConfiguracionUsuario()
        }
    }

    fun actualizarUsuario(
        usuario: Usuario,
        actualizacion: Usuario.() -> Unit,
    ): Usuario {
        return usuario.apply(actualizacion)
    }

    // Parte D: Función also

    fun crearUsuarioConLog(
        nombre: String,
        email: String,
        onLog: (String) -> Unit,
    ): Usuario {
        return Usuario(nombre = nombre).also {
            onLog("Usuario creado: ${it.nombre}")
        }.apply {
            this.email = email
        }.also {
            onLog("Email asignado: ${it.email}")
        }.apply {
            this.activo = true
        }.also {
            onLog("Usuario activado")
        }
    }

    fun crearYValidar(
        nombre: String,
        email: String,
    ): Pair<Usuario, Boolean> {
        var esValido = false
        val usuario = Usuario(nombre = nombre, email = email).also {
            esValido = it.nombre.isNotEmpty() && it.email.contains("@")
        }
        return usuario to esValido
    }

    // Parte E: Función let

    fun procesarEmailOpcional(email: String?): String {
        return email?.let { "Usuario con email: $it" } ?: "Usuario sin email"
    }

    fun generarMensajesBienvenida(usuarios: List<Usuario>): List<String> {
        return usuarios
            .filter { it.activo && it.email.isNotEmpty() }
            .map { usuario ->
                usuario.let { "Bienvenido/a ${it.nombre} (${it.email})" }
            }
    }

    // Parte F: Combinación de Scope Functions

    fun procesarUsuarioComplejo(datosBase: Map<String, String>): Usuario? {
        val nombre = datosBase["nombre"]
        val email = datosBase["email"]

        if (nombre.isNullOrEmpty() || email.isNullOrEmpty()) return null

        return Usuario().run {
            this.nombre = nombre
            this.email = email
            this
        }.apply {
            this.activo = true
        }.also { usuario ->
            if (datosBase["departamento"].equals("IT", ignoreCase = true)) {
                usuario.configuracion.tema = "oscuro"
                usuario.roles.add("IT_USER")
            }
        }
    }

    fun procesarLoteUsuarios(usuarios: List<Usuario>): List<Usuario> {
        return usuarios.map { usuario ->
            usuario.apply {
                activo = true
            }.also {
                if (it.roles.isEmpty()) it.roles.add("USER")
            }.apply {
                configuracion.notificaciones = true
            }.run {
                if (nombre.equals("Admin", ignoreCase = true)) {
                    roles.add("ADMIN")
                    configuracion.nivelPrivacidad = 3
                }
                this
            }
        }
    }

    fun parsearYCrearUsuario(datosRaw: String): Usuario? {
        if (datosRaw.isBlank() || !datosRaw.contains(":") || !datosRaw.contains("|")) {
            return null
        }

        return try {
            datosRaw
                .split("|")
                .filter { it.contains(":") }
                .associate { item ->
                    val partes = item.split(":", limit = 2)
                    partes[0].trim().lowercase() to partes[1].trim()
                }
                .let { mapa ->
                    val nombre = mapa["nombre"]
                    val email = mapa["email"]

                    if (nombre == null || email == null) {
                        null
                    } else {
                        Usuario()
                            .run {
                                this.nombre = nombre
                                this.email = email

                                mapa["id"]
                                    ?.toIntOrNull()
                                    ?.let { this.id = it }

                                mapa["activo"]
                                    ?.let { this.activo = it.toBoolean() }

                                this
                            }
                            .apply {
                                mapa["tema"]?.let {
                                    configuracion.tema = it
                                }
                            }
                            .also {
                                println("Usuario creado exitosamente: ${it.nombre}")
                            }
                    }
                }
        } catch (e: Exception) {
            null
        }
    }