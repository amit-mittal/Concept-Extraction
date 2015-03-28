package sample.criterias;

public class Concept
{
    public int true_positive;
    public int false_positive;
    public int false_negative;
    
    public Concept()
    {
        true_positive = 0;
        false_positive = 0;
        false_negative = 0;
    }
    
    public float findPrecision()
    {
        float p = (float) this.true_positive
                / (this.true_positive + this.false_positive);

        return p;
    }

    public float findRecall()
    {
        float r = (float) this.true_positive
                / (this.true_positive + this.false_negative);

        return r;
    }

    public float findF1Measure()
    {
        float p = findPrecision();
        float r = findRecall();

        float f1 = 2 * p * r / (p + r);

        return f1;
    }
}
