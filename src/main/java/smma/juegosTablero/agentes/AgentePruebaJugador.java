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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegosTablero.Vocabulario;
import static juegosTablero.Vocabulario.Motivo.PARTICIPACION_EN_JUEGOS_SUPERADA;
import static juegosTablero.Vocabulario.getOntologia;
import juegosTablero.dominio.elementos.Juego;
import juegosTablero.dominio.elementos.JuegoAceptado;
import juegosTablero.dominio.elementos.Jugador;
import juegosTablero.dominio.elementos.Motivacion;
import juegosTablero.dominio.elementos.ProponerJuego;
import smma.juegosTablero.gui.Consola;
import smma.juegosTablero.Constantes;

/**
 *
 * @author pedroj
 */
public class AgentePruebaJugador extends Agent implements Vocabulario, Constantes {
    // Generador de números aleatorios
    private final Random aleatorio = new Random();
    
    // Para la generación y obtención del contenido de los mensages
    private final ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private final Codec codec = new SLCodec();

    // Las ontología que utilizará el agente
    private Ontology ontologia;
    
    // Variables
    private TipoJuego tipoJuego;
    private Jugador jugador;
    private Consola guiConsola;

    @Override
    protected void setup() {
        // Inicialización variables
        guiConsola = new Consola(this);
        seleccionaJuego();
        jugador = new Jugador("prueba" + tipoJuego + "-" + getLocalName(), getAID());
        guiConsola.mensaje("Comienza la ejecución " + jugador);
         
        // Regisro de la Ontología
        try {
            ontologia = getOntologia(tipoJuego);
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgentePruebaJugador.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontologia);
        
        //Registro en páginas Amarrillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setType(TIPO_SERVICIO);
	sd.setName(NombreServicio.values()[tipoJuego.ordinal()+1].name());
	dfd.addServices(sd);
	
        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(AgentePruebaJugador.class.getName()).log(Level.SEVERE, null, ex);
        }
	
        // Plantilla para la tarea Completar Juego
        MessageTemplate mtProponerJuego = 
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        addBehaviour(new TareaProponerJuego(this, mtProponerJuego));
    }

    @Override
    protected void takeDown() {
        // Liberar recursos
        guiConsola.dispose();
        
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            Logger.getLogger(AgentePruebaJugador.class.getName()).log(Level.SEVERE, null, ex);
        }
	
        //Se despide
        System.out.println("Finaliza la ejecución de " + this.getName());
    }
    
    /**
     * Seleccionamos al azar el juego del agente
     */
    private void seleccionaJuego() {
        tipoJuego = TipoJuego.values()[aleatorio.nextInt(TipoJuego.values().length)];
    }
    
    /**
     * Toma la decisión para aceptar el juego
     * @return true si el agente quiere participar del juego
     */
    public boolean aceptarJugar(Juego juego) {
        int tiradaDado = aleatorio.nextInt(D10);
        guiConsola.mensaje("Tirada de dado: " + tiradaDado);
        return tiradaDado < AFIRMATIVA;
    }
    
    public void registrarJuego(ProponerJuego proponerJuego) {
        
    }
    
    class TareaProponerJuego extends ProposeResponder {
        
        public TareaProponerJuego(Agent agente, MessageTemplate mt) {
            super(agente, mt);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
            ACLMessage respuesta = propose.createReply();
            
            try {
                Action ac = (Action) manager.extractContent(propose);
                ProponerJuego proponerJuego = (ProponerJuego) ac.getAction();
                Juego juego = proponerJuego.getJuego();
                if ( aceptarJugar(juego) ) {
                    // Decisión afirmativa
                    registrarJuego(proponerJuego);
                    respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    JuegoAceptado juegoAceptado = new JuegoAceptado(juego, jugador);
                    manager.fillContent(respuesta, juegoAceptado);
                } else {
                    // Decisión negativa
                    respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);    
                    Motivacion motivacion = new Motivacion(juego, PARTICIPACION_EN_JUEGOS_SUPERADA);
                    manager.fillContent(respuesta, motivacion);
                }
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePruebaJugador.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            guiConsola.mensaje(respuesta.toString());
            
            return respuesta;
        }
    }
}
