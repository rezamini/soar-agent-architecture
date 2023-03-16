package com.soar.agent.architecture.rhs;

import java.util.List;

import org.jsoar.kernel.rhs.functions.AbstractRhsFunctionHandler;
import org.jsoar.kernel.rhs.functions.RhsFunctionContext;
import org.jsoar.kernel.rhs.functions.RhsFunctionException;
import org.jsoar.kernel.rhs.functions.RhsFunctions;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.Symbol;

import com.google.common.collect.Streams;

//count how many WMEs the identifier has
public class IdentifierSize extends AbstractRhsFunctionHandler {

    public IdentifierSize() {
        super("size", 1, 1);
    }

    @Override
    public Symbol execute(RhsFunctionContext context, List<Symbol> arguments) throws RhsFunctionException {
        RhsFunctions.checkArgumentCount(this, arguments);

        Identifier setId = arguments.get(0).asIdentifier();
        if (setId == null) {
            throw new RhsFunctionException(this.getName() + " was called with a non-identifer argument in rule "
                    + context.getProductionBeingFired());
        }
        
        long sizeCount = Streams.stream(setId.getWmes()).count();
        return context.getSymbols().createInteger(sizeCount);
    }

}
