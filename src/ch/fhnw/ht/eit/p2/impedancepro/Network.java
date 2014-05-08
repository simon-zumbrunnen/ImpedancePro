package ch.fhnw.ht.eit.p2.impedancepro;

import org.jfree.data.xy.XYDataset;

import ch.fhnw.ht.eit.p2.impedancepro.complex.ComplexNumber;
import ch.fhnw.ht.eit.p2.impedancepro.util.EngineeringUtil;

/**
 * @author Stephan Fahrni
 */

public class Network {
	private double frequency;
	private String frequencyString;
	private XYDataset swrData, reflectanceData, amplitudeData, amplitudeDBData;
	private double monteCarloResult;

	private MatchingNetwork[] matchingNetworks;
	private SourceLoadNetwork sourceNetwork;
	private SourceLoadNetwork loadNetwork;

	private ComplexNumber Zq;
	private ComplexNumber Zl;

	private ImpedanceProModel model;

	private double LSG1BT1, LSG1BT2, LSG2BT1, LSG2BT2, LSG3BT1, LSG3BT2,
			LSG4BT1, LSG4BT2;
	private int LSG1X11Error, LSG2X21Error, LSG3X32Error, LSG4X42Error;

	private MatchingNetwork solution1, solution2, solution3, solution4;

	public Network(ImpedanceProModel model) {
		this.model = model;
		sourceNetwork = new SourceLoadNetwork();
		loadNetwork = new SourceLoadNetwork();
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
		this.frequencyString = EngineeringUtil.convert(frequency, 2);
	}

	public String getFrequencyString() {
		return frequencyString;
	}

	public void setFrequencyString(String frequencyString) {
		this.frequency = EngineeringUtil.parse(frequencyString);
		this.frequencyString = EngineeringUtil.convert(frequency, 2);
	}

	public XYDataset getSwrData() {
		return swrData;
	}

	public void setSwrData(XYDataset swrData) {
		this.swrData = swrData;
	}

	public XYDataset getReflectanceData() {
		return reflectanceData;
	}

	public void setReflectanceData(XYDataset reflectanceData) {
		this.reflectanceData = reflectanceData;
	}

	public XYDataset getAmplitudeData() {
		return amplitudeData;
	}

	public void setAmplitudeData(XYDataset amplitudeData) {
		this.amplitudeData = amplitudeData;
	}

	public XYDataset getAmplitudeDBData() {
		return amplitudeDBData;
	}

	public void setAmplitudeDBData(XYDataset amplitudeDBData) {
		this.amplitudeDBData = amplitudeDBData;
	}

	public double getMonteCarloResult() {
		return monteCarloResult;
	}

	public void setMonteCarloResult(double monteCarloResult) {
		this.monteCarloResult = monteCarloResult;
	}

	public void calculateMatchingNetworks() {

		byte[] topology = new byte[4];

		double w = 0;

		// Initialize varbiable with zero

		double re = 0, a = 0, b = 0, c = 0;
		double X11 = 0, X12 = 0, X21 = 0, X22 = 0, X31 = 0, X32 = 0, X41 = 0, X42 = 0;
		double RS = 0, XS = 0, RL = 0, XL = 0;

		solution1 = null;
		solution2 = null;
		solution3 = null;
		solution4 = null;
		
		// Get load and source network

		Zq = new ComplexNumber();
		Zq = sourceNetwork.getImpedanceAtFrequency(frequency);

		Zl = new ComplexNumber();
		Zl = loadNetwork.getImpedanceAtFrequency(frequency);

		RS = Zq.getRe();
		XS = Zq.getIm();

		RL = Zl.getRe();
		XL = Zl.getIm();

		if (RS == RL) {
			solution1.setTopology(0000);
		}

		else {

			w = 2 * Math.PI * frequency;

			// In this part, solution 1 and 2 is calculated

			re = Zl.getRe();
			a = 1 - RS / re;

			b = XS;
			c = (Math.pow(RS, 2)) + (Math.pow(XS, 2));

			// Check, if there a imaginary part of solution 1
			// If there a imaginary part, component is not applicable to the
			// matched network

			if ((Math.pow(b, 2)) - (a * c) >= 0) {

				X11 = (-b + Math.sqrt((Math.pow(b, 2)) - a * c)) / (a);

				X12 = -((Math.pow(XS, 2) * X11 + XS * Math.pow(X11, 2) + Math
						.pow(RS, 2) * X11)
						/ (Math.pow(RS, 2) + Math.pow(XS, 2) + 2 * X11 * XS + Math
								.pow(X11, 2)) + Zl.getIm());

				// determine C or L of solution 1

				solution1 = new MatchingNetwork();

				if (X11 > 0) {

					LSG1BT1 = X11 / w;

					topology[0] = MatchingNetwork.PAR;
					topology[1] = MatchingNetwork.L;

				} else {

					LSG1BT1 = -1 / (w * X11);

					topology[0] = MatchingNetwork.PAR;
					topology[1] = MatchingNetwork.C;

				}

				// determine C or L of solution 1

				if (X12 == 0) {

					// only wire

					topology[2] = MatchingNetwork.EMPTY;
					topology[3] = MatchingNetwork.EMPTY;

				} else {

					if (X12 > 0) {

						LSG1BT2 = X12 / w;

						topology[2] = MatchingNetwork.SER;
						topology[3] = MatchingNetwork.L;

					} else {

						LSG1BT2 = -1 / (w * X12);

						topology[2] = MatchingNetwork.SER;
						topology[3] = MatchingNetwork.C;

					}

				}

				solution1.electricalComponents[0].setValue(LSG1BT1);
				solution1.electricalComponents[1].setValue(LSG1BT2);
				solution1.setTopology(byteArrayToInt(topology));

			} else {

				LSG1X11Error = 1;
			}

			// Check, if there a imaginary part of solution 2
			// If there a imaginary part, component is not applicable to the
			// matched network

			if ((Math.pow(b, 2)) - (a * c) >= 0) {

				X21 = (-b - Math.sqrt((Math.pow(b, 2)) - a * c)) / (a);

				X22 = -((Math.pow(XS, 2) * X21 + XS * Math.pow(X21, 2) + Math
						.pow(RS, 2) * X21)
						/ (Math.pow(RS, 2) + Math.pow(XS, 2) + 2 * X21 * XS + Math
								.pow(X21, 2)) + Zl.getIm());

				solution2 = new MatchingNetwork();

				// determine C or L of solution 2

				if (X21 > 0) {

					LSG2BT1 = X21 / w;

					topology[0] = MatchingNetwork.PAR;
					topology[1] = MatchingNetwork.L;

				} else {

					LSG2BT1 = -1 / (w * X21);

					topology[0] = MatchingNetwork.PAR;
					topology[1] = MatchingNetwork.C;

				}

				// determine C or L of solution 2

				if (X22 == 0) {

					// short circuit

					topology[2] = MatchingNetwork.EMPTY;
					topology[3] = MatchingNetwork.EMPTY;

				} else {

					if (X22 > 0) {

						LSG2BT2 = X22 / w;

						topology[2] = MatchingNetwork.SER;
						topology[3] = MatchingNetwork.L;

					} else {

						LSG2BT2 = -1 / (w * X22);

						topology[2] = MatchingNetwork.SER;
						topology[3] = MatchingNetwork.C;

					}
				}

				solution2.electricalComponents[0].setValue(LSG2BT1);
				solution2.electricalComponents[1].setValue(LSG2BT2);
				solution2.setTopology(byteArrayToInt(topology));

			} else {

				LSG2X21Error = 1;
			}

			// In this part, solution 3 and 4 is calculated

			re = Zq.getRe();

			a = 1 - RL / re;
			b = XL;
			c = Math.pow(RL, 2) + Math.pow(XL, 2);

			// Check, if there a imaginary part of solution 3
			// If there a imaginary part, component is not applicable to the
			// matched network

			if (Math.pow(b, 2) - (a * c) >= 0) {

				X32 = (-b + Math.sqrt(Math.pow(b, 2) - a * c)) / (a);

				X31 = -((Math.pow(XL, 2) * X32 + XL * Math.pow(X32, 2) + Math
						.pow(RL, 2) * X32)
						/ (Math.pow(RL, 2) + Math.pow(XL, 2) + 2 * X32 * XL + Math
								.pow(X32, 2)) + Zq.getIm());

				solution3 = new MatchingNetwork();

				// determine C or L of solution 3

				if (X31 == 0) {

					// short circuit

					topology[0] = MatchingNetwork.EMPTY;
					topology[1] = MatchingNetwork.EMPTY;

				} else {

					if (X31 > 0) {

						LSG3BT1 = X31 / w;

						topology[0] = MatchingNetwork.SER;
						topology[1] = MatchingNetwork.L;

					} else {

						LSG3BT1 = -1 / (w * X31);

						topology[0] = MatchingNetwork.SER;
						topology[1] = MatchingNetwork.C;
					}
				}

				// determine C or L of solution 3

				if (X32 > 0) {

					LSG3BT2 = X32 / w;

					topology[2] = MatchingNetwork.PAR;
					topology[3] = MatchingNetwork.C;

				} else {

					LSG3BT2 = -1 / (w * X32);

					topology[2] = MatchingNetwork.PAR;
					topology[3] = MatchingNetwork.L;
				}

				solution3.electricalComponents[0].setValue(LSG3BT1);
				solution3.electricalComponents[1].setValue(LSG3BT2);
				solution3.setTopology(byteArrayToInt(topology));

			} else {

				LSG3X32Error = 1;
			}

			// Check, if there a imaginary part of solution 4
			// If there a imaginary part, component is not applicable to the
			// matched network

			if (Math.pow(b, 2) - (a * c) >= 0) {

				X42 = (-b - Math.sqrt(Math.pow(b, 2) - a * c)) / (a);

				X41 = -((Math.pow(XL, 2) * X42 + XL * Math.pow(X42, 2) + Math
						.pow(RL, 2) * X42)
						/ (Math.pow(RL, 2) + Math.pow(XL, 2) + 2 * X42 * XL + Math
								.pow(X42, 2)) + Zq.getIm());

				solution4 = new MatchingNetwork();

				// determine C or L of solution 4

				if (X41 == 0) {

					// short circuit
					topology[0] = MatchingNetwork.EMPTY;
					topology[1] = MatchingNetwork.EMPTY;

				} else {

					if (X41 > 0) {

						LSG4BT1 = X41 / w;

						topology[0] = MatchingNetwork.SER;
						topology[1] = MatchingNetwork.L;

					} else {

						LSG4BT1 = -1 / (w * X41);

						topology[0] = MatchingNetwork.SER;
						topology[1] = MatchingNetwork.C;
					}
				}

				// determine C or L of solution 4

				if (X42 > 0) {

					LSG4BT2 = X42 / w;

					topology[2] = MatchingNetwork.PAR;
					topology[3] = MatchingNetwork.L;

				} else {

					LSG4BT2 = -1 / (w * X42);

					topology[2] = MatchingNetwork.PAR;
					topology[3] = MatchingNetwork.C;
				}

				solution4.electricalComponents[0].setValue(LSG4BT1);
				solution4.electricalComponents[1].setValue(LSG4BT2);
				solution4.setTopology(byteArrayToInt(topology));

			} else {

				LSG4X42Error = 1;
			}
		}

		matchingNetworks = new MatchingNetwork[] { solution1, solution2,
				solution3, solution4 };

		model.setChanged();
		model.notifyObservers();
	}

	public void calculateMonteCarlo() {

	}

	public MatchingNetwork[] getMatchingNetworks() {
		return matchingNetworks;
	}

	public void setMatchingNetworks(MatchingNetwork[] matchingNetworks) {
		this.matchingNetworks = matchingNetworks;
	}

	public SourceLoadNetwork getSourceNetwork() {
		return sourceNetwork;
	}

	public void setSourceNetwork(SourceLoadNetwork sourceNetwork) {
		this.sourceNetwork = sourceNetwork;
	}

	public SourceLoadNetwork getLoadNetwork() {
		return loadNetwork;
	}

	public void setLoadNetwork(SourceLoadNetwork loadNetwork) {
		this.loadNetwork = loadNetwork;
	}

	private int byteArrayToInt(byte[] encodedValue) {
		int value = 0;
		
		for (int i = 0; i < encodedValue.length; i++) {
			value += encodedValue[i] * Math.pow(10, i);
		}
		
		return value;
	}
}
