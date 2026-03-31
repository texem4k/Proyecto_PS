import java.util.Date


class Tarea (
    var nombre: String? = null,
    var descripcion: String? = null,
    var prioridad: Int? = null,
    var tags: MutableList<Tags>? = null,
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

    fun establecerPrioridad(prioridad:Int){
        this.prioridad = prioridad;
    }

    fun establecertags(tag:String){
        if (this.tags==null){this.tags = mutableListOf();}
        tags.add(tag);
    }

    fun establecerTopicos(topico:Topico) {
        this.topico = topico;
    }

    fun establecerFechaInicio(fechInicial: Date){
        this.fechaInicio = fechInicial;
    }

    fun establecerFechaFinal(fechFinal: Date){
        this.fechaFinal = fechFinal;
    }

    fun establecerDuracion(duracion:Int){
        this.duracion = duracion;
    }
}