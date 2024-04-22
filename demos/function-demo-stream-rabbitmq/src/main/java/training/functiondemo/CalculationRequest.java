package training.functiondemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {

    private double a;
    private double b;

    public CalculationResponse calculate() {
        return new CalculationResponse(a + b);
    }
}
