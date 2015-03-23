package sample.criterias;

public class Criteria
{
    private int true_positives;
    private int true_negatives;
    private int false_positives;
    private int false_negatives;

    public Criteria(int tp, int tn, int fp, int fn)
    {
        this.true_positives = tp;
        this.true_negatives = tn;
        this.false_positives = fp;
        this.false_negatives = fn;
    }

    public float FindPrecision()
    {
        float p = (float) this.true_positives
                / (this.true_positives + this.false_positives);

        return p;
    }

    public float FindRecall()
    {
        float r = (float) this.true_positives
                / (this.true_positives + this.false_negatives);

        return r;
    }

    public float FindF1Measure()
    {
        float p = FindPrecision();
        float r = FindRecall();

        float f1 = 2 * p * r / (p + r);

        return f1;
    }

    public float FindAccuracy()
    {
        int total = false_negatives + false_positives + true_negatives
                + true_positives;
        float a = (float) (true_negatives + true_positives) / total;

        return a;
    }
}
