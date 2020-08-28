package iftm.service;

import bitflow4j.AbstractProcessingStep;
import bitflow4j.Sample;
import iftm.anomalydetection.IFTMAnomalyDetection;
import iftm.errorfunction.L2NormModelResultError;
import iftm.errorfunction.RootMeanSquaredError;
import iftm.identityfunction.*;
import iftm.thresholdmodel.ExponentialMovingAvgStd;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class AnomalyDetection extends AbstractProcessingStep {

    private IFTMAnomalyDetection model = null;
    private final String modelType;
    Logger logger = Logger.getLogger(AnomalyDetection.class.getName());

    /**
     *
     * @param modelType expected to be arima, lstm, ae, aelstm, or vae, cabirch
     * @throws IOException
     * @throws URISyntaxException
     */
    public AnomalyDetection(String modelType) {
        this.modelType = modelType;
    }

    @Override
    public void handleSample(Sample sample) throws IOException {
        if (model == null) {
            switch (modelType) {
                case "arima":
                    model = new IFTMAnomalyDetection(new OnlineArimaMultiIdentityFunction(50, 3, 0.5, 10), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
                case "ae":
                    model = new IFTMAnomalyDetection(new AutoencoderIdentityFunction(2, 0.01), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
                case "aelstm":
                    model = new IFTMAnomalyDetection(new AutoencoderLSTMIdentityFunction(2, 0.001D), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
                case "vae":
                    model = new IFTMAnomalyDetection(new VariationalAutoencoderIdentityFunction(2, 0.001D), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
                case "lstm":
                    model = new IFTMAnomalyDetection(new LSTMIdentityFunction(2, 0.001D), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
                case "cabirch":
                    model = new IFTMAnomalyDetection(new CABirchIdentityFunction(), new L2NormModelResultError(), new ExponentialMovingAvgStd(0.01));
                    break;
                default:
                    model = new IFTMAnomalyDetection(new OnlineArimaMultiIdentityFunction(50, 3, 0.5, 10), new RootMeanSquaredError(), new ExponentialMovingAvgStd(0.01));
                    break;
            }
        }

        boolean isAnomaly = model.predict(sample.getMetrics()).isAnomaly();
        model.train(sample.getMetrics());
        sample.setTag("is_anomaly",""+isAnomaly);
        logger.info("is_anomaly: "+isAnomaly);
        output(sample);
    }

}
