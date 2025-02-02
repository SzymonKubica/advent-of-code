package solutions.day13;

import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day13 implements Solution {

    @Override
    public void firstPart(String inputFile) {

        List<Triple<Button, Button, Point>> machines = parseClawMachines(Utils.readInputAsStream(inputFile));
        System.out.println(Utils.toStringLineByLine(machines));
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private List<Triple<Button, Button, Point>> parseClawMachines(Stream<String> input) {
        String allInput = input.collect(Collectors.joining("\n"));

        String[] machineConfigurations = allInput.split("\n\n");

        List<Triple<Button, Button, Point>> parsedConfigurations = new ArrayList<>();
        for (String config : machineConfigurations) {

            parsedConfigurations.add(parseConfiguration(config));
        }
        return parsedConfigurations;
    }

    private Triple<Button, Button, Point> parseConfiguration(String config) {
        List<String> lines = config.lines().toList();
        assert (lines.size() == 3) : "A correct claw machine config specifies buttons A, B and the prize location";
        String buttonAConfig = lines.get(0);
        String buttonBConfig = lines.get(1);
        String prizeConfig = lines.get(2);

        return Triple.of(parseButton(buttonAConfig), parseButton(buttonBConfig), parsePrize(prizeConfig));
    }

    private Button parseButton(String buttonConfig) {
        String[] parts = buttonConfig.split(" ");
        assert (parts.length == 4) : "Button config needs to be of the form: 'Button B: X+27, Y+71'";

        int xDisplacement = Integer.parseInt(parts[2].split("\\+")[1].split(",")[0]);
        int yDisplacement = Integer.parseInt(parts[3].split("\\+")[1]);

        String buttonType = parts[1].split(":")[0];

        int cost = buttonType.equals("A") ? 3 : 1;
        return new Button(cost, new Point(xDisplacement, yDisplacement));
    }

    private Point parsePrize(String prizeConfig) {
        String[] parts = prizeConfig.split(" ");
        assert (parts.length == 3) : "Prize config needs to be of the form: 'Prize: X=18641, Y=10279'";

        int x = Integer.parseInt(parts[1].split("=")[1].split(",")[0]);
        int y = Integer.parseInt(parts[2].split("=")[1]);

        return new Point(x, y);
    }


    record Button(int cost, Point clawMovement) {
    }
}
