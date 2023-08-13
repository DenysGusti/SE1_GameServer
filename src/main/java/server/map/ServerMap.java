package server.map;

import java.util.HashMap;
import java.util.Map;

public class ServerMap implements Cloneable {
	private Map<Coordinate, ServerTile> map;

	public ServerMap(final Map<Coordinate, ServerTile> map) {
        this.map = map;
    }

	public ServerMap() {
		this.map = new HashMap<Coordinate, ServerTile>();
	}

	public Map<Coordinate, ServerTile> getMap(){
		return map;
	}
	
	@Override
	public Object clone() {
		ServerMap clonedMap = null;
	    try {
	        clonedMap = (ServerMap) super.clone();
	    } catch (CloneNotSupportedException e) {
	    	clonedMap = new ServerMap(this.map);
	    }
	    
	    final Map<Coordinate, ServerTile> copiedMap = new HashMap<>();
    	for (Map.Entry<Coordinate, ServerTile> entry : this.map.entrySet()) {
    		copiedMap.put(entry.getKey(), (ServerTile) entry.getValue().clone());
        }
    	
    	clonedMap.map = copiedMap;
    	
	    return clonedMap;
	}
}
