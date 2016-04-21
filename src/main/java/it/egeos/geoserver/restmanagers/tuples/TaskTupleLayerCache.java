package it.egeos.geoserver.restmanagers.tuples;


public class TaskTupleLayerCache {
	protected static Long ABORTED_STATUS=-1L;
	protected static Long PENDING_STATUS=0L;
	protected static Long RUNNING_STATUS=1L;
	protected static Long DONE_STATUS=2L;
	
	public long tilesProcessed;
	public long totalTiles;
	public long remainingTiles;
	public long id;
	public long status;
	
	public TaskTupleLayerCache(){
		super();
	}

	public TaskTupleLayerCache(long tilesProcessed, long totalTiles,
			long remainingTiles, long id, long status) {
		super();
		this.tilesProcessed = tilesProcessed;
		this.totalTiles = totalTiles;
		this.remainingTiles = remainingTiles;
		this.id = id;
		this.status = status;
	}

	public long getTilesProcessed() {
		return tilesProcessed;
	}

	public void setTilesProcessed(long tilesProcessed) {
		this.tilesProcessed = tilesProcessed;
	}

	public long getTotalTiles() {
		return totalTiles;
	}

	public void setTotalTiles(long totalTiles) {
		this.totalTiles = totalTiles;
	}

	public long getRemainingTiles() {
		return remainingTiles;
	}

	public void setRemainingTiles(long remainingTiles) {
		this.remainingTiles = remainingTiles;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "TaskTupleLayerCache [tilesProcessed=" + tilesProcessed
				+ ", totalTiles=" + totalTiles + ", remainingTiles="
				+ remainingTiles + ", id=" + id + ", status=" + status + "]";
	}
	
	
}
