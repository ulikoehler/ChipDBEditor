package uli.chipdbeditor;

/*
 * NumberSpinner.java
 * A spinner with easier number access
 * Created on 28.02.2009, 19:22:18
 */
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author uli
 */
public class NumberSpinner extends JSpinner {

    /** Creates new form BeanForm */
    public NumberSpinner() {
        initComponents();
    }

    public void setMinimum(Comparable min) {
        ((SpinnerNumberModel) getModel()).setMinimum(min);
    }

    public void setMaximum(Comparable max) {
        ((SpinnerNumberModel) getModel()).setMaximum(max);
    }

    public int getIntValue() {
        return ((SpinnerNumberModel) getModel()).getNumber().intValue();
    }

    public void setIntValue(int value) {
        ((SpinnerNumberModel) getModel()).setValue(value);
    }

    public long getLongValue() {
        return ((SpinnerNumberModel) getModel()).getNumber().longValue();
    }

    public void setLongValue(long value) {
        ((SpinnerNumberModel) getModel()).setValue(value);
    }

    public float getFloatValue() {
        return ((SpinnerNumberModel) getModel()).getNumber().floatValue();
    }

    public void setFloatValue(float value) {
        ((SpinnerNumberModel) getModel()).setValue(value);
    }

    public double getDoubleValue() {
        return ((SpinnerNumberModel) getModel()).getNumber().doubleValue();
    }

    public void setDoubleValue(double value) {
        ((SpinnerNumberModel) getModel()).setValue(value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     *
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
