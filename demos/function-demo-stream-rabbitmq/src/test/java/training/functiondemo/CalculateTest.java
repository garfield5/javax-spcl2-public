package training.functiondemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CalculateTest {

    @Autowired
    FunctionCatalog functionCatalog;

    @Test
    void calculate() {
        var function = (Function<CalculationRequest, CalculationResponse>) functionCatalog.lookup("calculate");
        var response = function.apply(new CalculationRequest(1.1234, 2.1234));
        assertEquals(3.2468, response.getResult(), 0.00005);
    }
}
