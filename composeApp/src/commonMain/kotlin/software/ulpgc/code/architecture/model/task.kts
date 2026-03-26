import java.util.Date


class Tarea (
    var nombre: String? = null,
    var descripcion: String? = null,
    var prioridad: Int? = null,
    var tags: List<Tags>? = null,
    var topico: Topico? = null,
    var fechaInicio: Date? = null,
    var fechaFinal: Date? = null,
    var duracion: Int?=null
    ){

    fun establecerNombre(nombre:String){
        this.nombre = nombre
    }

    fun establecerDescripcion(desc:String){
        this.descripcion = desc
    }


}