package edu.cmu.tetrad.util;

public class GaussianPower extends NormalDistribution {
    static final long serialVersionUID = 23L;

	private double power;
	private String name;
	@Override
	public String getName(){
		return this.name;
	}

	public GaussianPower(double power){
		this(0,1,power);
	}
	
	public GaussianPower(double mean, double sd, double power){
		super(mean,sd);
		this.power = power;
		this.name = "N^"+power+"("+mean+","+sd+")";
	}

	@Override
	public double nextRandom() {
		double value = super.nextRandom();
		double poweredValue = java.lang.Math.pow(java.lang.Math.abs(value),power);
        return (value >= 0) ? poweredValue : -poweredValue;
    }

	
}
