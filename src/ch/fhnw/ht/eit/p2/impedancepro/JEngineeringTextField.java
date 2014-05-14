package ch.fhnw.ht.eit.p2.impedancepro;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * @author Richard Gut
 * @author Simon Zumbrunnen
 * 
 */

public class JEngineeringTextField extends JTextField implements FocusListener {
	private static final long serialVersionUID = 1L;
	private JEngineeringTextField txtField = this;
	private double minValue = -Double.MAX_VALUE, maxValue = Double.MAX_VALUE,
			value;
	private boolean emptyAllowed = false;
	private int digits = 1;
	private boolean edited = false;
	private boolean errorDisplayed = false;

	public JEngineeringTextField(int col) {
		super(col);
		init();
	}

	public JEngineeringTextField(int digits, int col) {
		super(col);
		if (digits < 3 || digits > 16) {
			throw new IllegalArgumentException();
		}
		this.digits = digits;
		init();
	}

	private void init() {
		addKeyListener(new KeyAdapter() {

			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() != KeyEvent.VK_ENTER)
					edited = true;
				char caracter = e.getKeyChar();
				int offs = txtField.getCaretPosition();

				String tfText = txtField.getText().substring(0, offs)
						+ caracter + txtField.getText().substring(offs);

				try {
					if (caracter == '-' || caracter == '+' || caracter == 'e') {
						EngineeringUtil.parse(tfText.trim() + "1");
					} else {
						EngineeringUtil.parse(tfText.trim());
					}
				} catch (Exception ex) {
					e.consume();
				}
			}
		});
		addFocusListener(this);
	}
	
	public boolean verify() {
		double v = 0.0;
		if (txtField.getText().isEmpty() && isEmptyAllowed()) {
			return true;
		} else if (txtField.getText().isEmpty() && !isEmptyAllowed()) {
			errorMsg();
			return false;
		} else {
			try {
				v = EngineeringUtil.parse(txtField.getText());
			} catch (NumberFormatException e) {
				errorMsg();
				return false;
			}
			if (v > maxValue || v < minValue) {
				errorMsg();
				return false;
			} else {
				if (edited) {
					setValue(v);
					edited = false;
				}

				return true;
			}
		}
	}

	public void setValue(double value) {
		this.value = value;
		edited = false;
		setText(EngineeringUtil.convert(value, digits));
	}

	public double getValue() {
		return value;
	}

	public void setRange(double minValue, double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		TooltipManager.setTooltip(
				txtField,
				"" + EngineeringUtil.convert(minValue, 2)
						+ " \u2264 Eingabe \u2264 "
						+ EngineeringUtil.convert(maxValue, 2),
				TooltipWay.down, 0);
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
		TooltipManager.setTooltip(txtField,
				"" + EngineeringUtil.convert(minValue, 2) + " \u2264 Eingabe",
				TooltipWay.down, 0);
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		TooltipManager.setTooltip(txtField,
				"Eingabe \u2264 " + EngineeringUtil.convert(maxValue, 2),
				TooltipWay.down, 0);
	}

	public boolean isEmptyAllowed() {
		return emptyAllowed;
	}

	public void setEmptyAllowed(boolean emptyAllowed) {
		this.emptyAllowed = emptyAllowed;
	}

	public void focusGained(FocusEvent e) {
		selectAll();
	}

	public void focusLost(FocusEvent e) {
		fireActionPerformed();
	}

	protected void fireActionPerformed() {
		if (verify())
			super.fireActionPerformed();
	}

	private void errorMsg() {
		if (errorDisplayed)
			return;
		errorDisplayed = true;
		final Color color = getBackground();
		setBackground(ImpedanceProView.LIGHT_RED);
		requestFocus();
		javax.swing.Timer timer = new javax.swing.Timer(1500,
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						setBackground(color);
						setText(EngineeringUtil.convert(value, digits));
						edited = false;
						errorDisplayed = false;
					}
				});
		timer.setRepeats(false);
		timer.start();
	}
}
