package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractIA;

/**
 * Created by Locos on 18/09/2015.
 */
public class Blank extends AbstractIA {

    public Blank(Fight fight, Fighter fighter) {
        super(fight, fighter, (byte) 1);
    }

    @Override
    public void apply() {
        this.stop = true;
    }
}
