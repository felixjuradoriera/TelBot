package Dto;

import java.time.LocalDateTime;

public class Alerta {
	
		
	private String bookie;
	private Double ratingFrom;
	private LocalDateTime fechaActivacion;
	
	private Double cuotaMax=150.0;
	private Double cuotaMin=0.0;
	
	private Integer diasAntelacion;
	
	private Double liquidezMinima;
			
	private String chatIdEnviar;

	public String getBookie() {
		return bookie;
	}  

	public void setBookie(String bookie) {
		this.bookie = bookie;
	}

	public Double getRatingFrom() {
		return ratingFrom;
	}

	public void setRatingFrom(Double ratingFrom) {
		this.ratingFrom = ratingFrom;
	}

	public LocalDateTime getFechaActivacion() {
		return fechaActivacion;
	}

	public void setFechaActivacion(LocalDateTime fechaActivacion) {
		this.fechaActivacion = fechaActivacion;
	}

	public Double getCuotaMax() {
		return cuotaMax;
	}

	public void setCuotaMax(Double cuotaMax) {
		this.cuotaMax = cuotaMax;
	}

	public Double getCuotaMin() {
		return cuotaMin;
	}

	public void setCuotaMin(Double cuotaMin) {
		this.cuotaMin = cuotaMin;
	}

	public Integer getDiasAntelacion() {
		return diasAntelacion;
	}

	public void setDiasAntelacion(Integer diasAntelacion) {
		this.diasAntelacion = diasAntelacion;
	}

	public Double getLiquidezMinima() {
		return liquidezMinima;
	}

	public void setLiquidezMinima(Double liquidezMinima) {
		this.liquidezMinima = liquidezMinima;
	}

	public String getChatIdEnviar() {
		return chatIdEnviar;
	}

	public void setChatIdEnviar(String chatIdEnviar) {
		this.chatIdEnviar = chatIdEnviar;
	}
	
	
	
	
	

}
