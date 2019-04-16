/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.agentes;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegosTablero.Vocabulario;
import static juegosTablero.Vocabulario.Motivo.JUEGOS_ACTIVOS_SUPERADOS;
import static juegosTablero.Vocabulario.Motivo.TIPO_JUEGO_NO_IMPLEMENTADO;
import static juegosTablero.Vocabulario.NombreServicio.GRUPO_JUEGOS;
import static juegosTablero.Vocabulario.TIPO_SERVICIO;
import static juegosTablero.Vocabulario.getOntologia;
import juegosTablero.dominio.elementos.CompletarJuego;
import juegosTablero.dominio.elementos.Grupo;
import juegosTablero.dominio.elementos.Juego;
import juegosTablero.dominio.elementos.JuegoAceptado;
import juegosTablero.dominio.elementos.Motivacion;
import smma.juegosTablero.gui.Consola;
import smma.juegosTablero.Constantes;
import static smma.juegosTablero.Constantes.AFIRMATIVA;
import static smma.juegosTablero.Constantes.D10;

/**
 *
 * @author pedroj
 */
public class AgentePruebaGrupoJuegos extends Agent implements Vocabulario, Constantes {
    // Generador de números aleatorios
    private final Random aleatorio = new Random();
    
    // Para la generación y obtención del contenido de los mensages
    private ContentManager[] manager;
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private final Codec codec = new SLCodec();

    // Las ontología que utilizará el agente
    private Ontology[] listaOntologias;
    
    // Constantes
    private final int JUEGOS_IMPLEMENTADOS = 2; //Implementamos dos juegos
    
    // Variables
    private TipoJuego[] listaJuegos;
    private Grupo grupoJuegos;
    private Consola guiConsola;

    @Override
    protected void setup() {
        // Inicialización variables
        grupoJuegos = new Grupo("pruebaGrupoJuegos-" + getLocalName(), getAID());
        guiConsola = new Consola(this);
        guiConsola.mensaje("Comienza la ejecución " + grupoJuegos);
        
        juegosImplementados();
        
        // Registro de las ontologías para los juegos de tablero
        listaOntologias = new Ontology[listaJuegos.length];
        manager = new ContentManager[listaJuegos.length];
        try {
            for ( int i = 0; i < listaJuegos.length; i++ ) {
                listaOntologias[i] = getOntologia(listaJuegos[i]);
                manager[i] = (ContentManager) getContentManager();
                manager[i].registerLanguage(codec);
                manager[i].registerOntology(listaOntologias[i]);
            }
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
            this.doDelete();
        }
        
        //Registro en páginas Amarrillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setType(TIPO_SERVICIO);
	sd.setName(GRUPO_JUEGOS.name());
	dfd.addServices(sd);
	
        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(AgentePruebaGrupoJuegos.class.getName()).log(Level.SEVERE, null, ex);
        }
	
        // Plantilla para la tarea Completar Juego
        MessageTemplate mtCompletarJuego = 
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        addBehaviour(new TareaCompletarJuego(this, mtCompletarJuego));
    }

    @Override
    protected void takeDown() {
        // Liberar recursos
        guiConsola.dispose();
        
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            Logger.getLogger(AgentePruebaGrupoJuegos.class.getName()).log(Level.SEVERE, null, ex);
        }
	
        //Se despide
        System.out.println("Finaliza la ejecución de " + this.getName());
    }
    
    /**
     * Eliminamos uno de los juegos disponibles al azar para simular las
     * posibles respuesta del agente a un juego no implementado
     */
    private void juegosImplementados() {
        int eliminado = aleatorio.nextInt(TipoJuego.values().length);
        
        listaJuegos = new TipoJuego[JUEGOS_IMPLEMENTADOS];
        
        int j = 0;
        for ( int i = 0; i < TipoJuego.values().length; i++ )
            if ( i != eliminado ) {
                listaJuegos[j] = TipoJuego.values()[i];
                j++;
            }
             
        guiConsola.mensaje("Juegos implementados: " + Arrays.toString(listaJuegos));
    }
    
    private int buscarIndiceJuego(String nombreOntologia) {
        int resultado = NO_HAY_ELEMENTO;
        
        boolean encontrado = false;
        int indice = 0;
        while ( (indice < listaJuegos.length) && !encontrado ) {
            if ( listaOntologias[indice].getName().equals(nombreOntologia) ) {
                encontrado = true;
                resultado = indice;
            } else {
                indice++;
            } 
        }
        
        return resultado;
    }
    
    public boolean aceptarJuego(Juego juego) {
        int tiradaDado = aleatorio.nextInt(D10);
        guiConsola.mensaje("Tirada de dado: " + tiradaDado);
        return tiradaDado < AFIRMATIVA;
    }
    
    public void registrarJuego(CompletarJuego completarJuego) {
        
    }
    
    class TareaCompletarJuego extends ProposeResponder {
        
        public TareaCompletarJuego(Agent agente, MessageTemplate mt) {
            super(agente, mt);
        }
        
        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
            ACLMessage respuesta = propose.createReply();
            int indiceJuego = buscarIndiceJuego(respuesta.getOntology());
            
            if ( indiceJuego == NO_HAY_ELEMENTO ) {
                guiConsola.mensaje("EXCEPCION " + TIPO_JUEGO_NO_IMPLEMENTADO);
                throw new NotUnderstoodException(TIPO_JUEGO_NO_IMPLEMENTADO.name());   
            } else {
                guiConsola.mensaje("Indice juego " + indiceJuego + " " + listaJuegos[indiceJuego]);
                
                try {
                    Action ac = (Action) manager[indiceJuego].extractContent(propose);
                    CompletarJuego completarJuego = (CompletarJuego) ac.getAction();
                    Juego juego = completarJuego.getJuego();
                    if ( aceptarJuego(juego) ) {
                        // Aceptamos
                        registrarJuego(completarJuego);
                        respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        JuegoAceptado juegoAceptado = new JuegoAceptado(juego, grupoJuegos);
                        
                        manager[indiceJuego].fillContent(respuesta, juegoAceptado);
                        
                    } else {
                        // Rechazamos
                        respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        Motivacion motivacion = new Motivacion(juego, JUEGOS_ACTIVOS_SUPERADOS);
                        
                        manager[indiceJuego].fillContent(respuesta, motivacion);
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePruebaGrupoJuegos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            guiConsola.mensaje(respuesta.toString());
            
            return respuesta;
        }
    }
}
