package it.polito.tdp.poweroutages.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.poweroutages.db.PowerOutagesDAO;

public class Model {
	
	private SimpleWeightedGraph<Nerc, DefaultWeightedEdge> graph;
	private NercIdMap nIdMap;
	private PowerOutagesDAO dao = new PowerOutagesDAO();
	
	
	
	
	public Model(){
		nIdMap = new NercIdMap();
	}
	
	
	
	
	public List<Nerc> getNercs() {
		return dao.loadAllNercs(nIdMap);
	}
	
	
	
	
	
	public void creaGrafo(){
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		List<Nerc> nercs = dao.loadAllNercs(nIdMap);
		Graphs.addAllVertices(graph, nercs);
		
		for(Nerc nerc : nercs){
			Set<Nerc> neighbors = dao.getNeighbors(nIdMap,nerc);
			
			for(Nerc neighbor : neighbors){
				int correlation = dao.getCorrelation(nerc, neighbor);		//ottengo il peso di ciascun arco
				DefaultWeightedEdge e = graph.getEdge(nerc, neighbor);
				if (e == null) {
					Graphs.addEdgeWithVertices(graph, nerc, neighbor, correlation);
				}
			}

		}
		
		System.out.println("Graphs vertex set: " + graph.vertexSet().size());
		System.out.println("Graphs edge set: " + graph.edgeSet().size());
		
	}

	
	
	
	/**
	 * Metodo che mi permette di trovare la lista dei vicini
	 * @param nerc
	 * @return lista di Nerc
	 */
	public List<NeighborNerc> getCorrelatedNeighbors(Nerc nerc) {
		List<NeighborNerc> correlatedNercs = new ArrayList<NeighborNerc>();
		List<Nerc> neighbors = Graphs.neighborListOf(graph, nerc);
		
		for(Nerc neighbor : neighbors){
			DefaultWeightedEdge edge = graph.getEdge(nerc, neighbor);
			correlatedNercs.add(new NeighborNerc(neighbor,(int) graph.getEdgeWeight(edge)));
		}
		
		Collections.sort(correlatedNercs);		//ordino la lista, nella classe NeighboNerc c'è un implements Comparable
		return correlatedNercs;
		
	}
	
	
	
	public void simula(int k) {
		Simulatore sim=new Simulatore();
		sim.init(k,dao.loadAllPowerOutages(nIdMap), nIdMap, graph);
		sim.run();
	}
	
	
	
	

}
