package sample.criterias;

public class Criteria
{
    public static float FindPrecision(float true_positives, float false_positives)
    {
        float p = true_positives / (true_positives + false_positives);

        return p;
    }

    public static float FindRecall(float true_positives, float false_negatives)
    {
        float r = true_positives / (true_positives + false_negatives);

        return r;
    }

    public static float FindF1Measure(float p, float r)
    {
        float f1 = (2 * p * r) / (p + r);

        return f1;
    }
}
