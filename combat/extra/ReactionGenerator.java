package extra;

import java.util.Random;

public class ReactionGenerator {

    Random randomGen = new Random();

    public double nextGaussian(double mu, double sigma) {
        return mu + randomGen.nextGaussian() * sigma;
    }

    public double nextExponential(double lambda) {
        return -Math.log(1 - randomGen.nextDouble()) / lambda;
    }

    public double nextExGaussian(double mu, double sigma, double lambda) {
        return nextGaussian(mu, sigma) + nextExponential(lambda);
    }

    public double nextUniform(double minimum, double maximum) {
        return randomGen.nextDouble() * (maximum - minimum) + minimum;
    }

    public double nextReactionTime(double mu, double sigma, double lambda, double p, double minimum, double maximum) {
        if (randomGen.nextDouble() < p) {
            return nextUniform(minimum, maximum);
        }

        double exGaussian = nextExGaussian(mu, sigma, lambda);
        if (exGaussian < minimum || exGaussian > maximum) {
            return nextReactionTime(mu, sigma, lambda, p, minimum, maximum);
        }

        return exGaussian;
    }
}