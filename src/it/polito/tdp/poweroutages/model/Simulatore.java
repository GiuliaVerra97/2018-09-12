package it.polito.tdp.poweroutages.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Simulatore {
	
	//modello lo stato del sistema
	private Graph<Nerc, DefaultWeightedEdge> grafo;
	private List<PowerOutage> powerLista;			//lista di black-out
	private Map<Nerc, Set<Nerc>> prestiti;
	
	//parametri simulazione
	private int k;
	
	//valori in output
	private int CATASTROFI;
	private Map<Nerc, Long> bonus;
	
	//coda
	private PriorityQueue<Evento> queue;
	
	
	public void init(int k, List<PowerOutage> powerLista, NercIdMap nercMap, Graph<Nerc, DefaultWeightedEdge> grafo) {
		this.queue=new PriorityQueue<Evento>();
		this.bonus=new HashMap<Nerc, Long>();
		this.prestiti=new HashMap<Nerc, Set<Nerc>>();
		
		for(Nerc n:nercMap.values()){
			this.bonus.put(n, Long.valueOf(0));		//inserisco nelle mappe bonus e prestiti solo i nerc (solo le chiavi) senza il valore
			this.prestiti.put(n, new HashSet<Nerc>());
		}
		
		this.CATASTROFI=0;
		this.k=k;
		this.powerLista=powerLista;
		this.grafo=grafo;
		
		
		//inserisco gli evennti iniziali
		for(PowerOutage po:this.powerLista) {
			Evento e=new Evento(Evento.TIPO.INIZIO_INTTERRUZIONE, po.getNerc(), null, po.getInizio(), po.getInizio(), po.getFine());
			queue.add(e);
		}
		
	}
	
	
	
	
	
	
	
	public void run() {
		
		while(!queue.isEmpty()) {
			
			Evento e=queue.poll();
			
			switch(e.getTipo()) {
			
			case INIZIO_INTTERRUZIONE:
								
				//cerca se c'è un donatore, atrimenti catastrofe
				Nerc donatore=null;
				Nerc nerc=e.getNerc();
				
				System.out.println("INIZIO INTERRUZIONE nerc:"+ nerc);
				
				if(this.prestiti.get(nerc).size()>0) {		//se ha dei possibili donatori scelgo tra i miei debitori

					double min=Long.MAX_VALUE;			//imposto min come il valore più grande possibile
					
					for(Nerc n: this.prestiti.get(nerc)) {		//restituisce una lista di Nerc donatori
						
						DefaultWeightedEdge edge=this.grafo.getEdge(nerc, n);
						
						if(this.grafo.getEdgeWeight(edge)<min) {		//se l'arco ha peso minore 
							if(!n.getStaPrestando()) {
								donatore=n;
								min=this.grafo.getEdgeWeight(edge);
							}
						}
					}
					
				} else {		//scorro tutti i vicini se non ci sono debitori 
					
					double min=Long.MAX_VALUE;
					
					List<Nerc> neighbors=Graphs.neighborListOf(this.grafo, nerc);
					
					for(Nerc n:neighbors) {
						
						DefaultWeightedEdge edge=this.grafo.getEdge(nerc, n);
						
						if(this.grafo.getEdgeWeight(edge)<min) {
							if(!n.getStaPrestando()) {
								donatore=n;
								min=this.grafo.getEdgeWeight(edge);
							}
						}
					}
					
				}
				
				//se non c'è nessun donatore c'è una catastrofe
				if(donatore!=null) {
					
					System.out.println("\n Trovato donatore"+donatore);
					
					donatore.setStaPrestando(true);
					Evento fine=new Evento(Evento.TIPO.FINE_INTERRUZIONE, e.getNerc(), donatore, e.getDataFine(), e.getDataInizio(), e.getDataFine());
					queue.add(fine);
					this.prestiti.get(donatore).add(e.getNerc());		//aggiungo nerc alla lista dei prestiti
					Evento cancella=new Evento(Evento.TIPO.CANCELLA_PRESTITO, e.getNerc(), donatore, e.getData().plusMonths(k), e.getDataInizio(), e.getDataFine());
					this.queue.add(cancella);
				
				}else {
					
					//catastrofe
					System.out.println("CATASTROFE!!!");
					this.CATASTROFI++;
				}
				
				
				break;
				
			case FINE_INTERRUZIONE:
				
				System.out.println("FINE INTERRUZIONE nerc: "+e.getNerc());
				
				//assegnare un bonus al donatore
				if(e.getDonatore()!=null) {
					this.bonus.put(e.getDonatore(), bonus.get(e.getDonatore())+Duration.between(e.getDataInizio(), e.getDataFine()).toDays());
				}
				
				//dire che il donatore non sta più prestando
				e.getDonatore().setStaPrestando(false);
				break;
				
			case CANCELLA_PRESTITO:
				
				System.out.println("Cancellazione PRESTITO: "+e.getDonatore()+" - "+e.getNerc());
				
				this.prestiti.remove(e.getDonatore()).remove(e.getNerc());
				break;
			
			}
		}
	
}	
	
	
	
	

}
