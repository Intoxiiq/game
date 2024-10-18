package soufix.fight.ia;

import soufix.fight.Fight;
import soufix.fight.Fighter;

public interface IA
{
  Fight getFight();
  Fighter getFighter();
  boolean isStop();
  void setStop(boolean stop);
  void addNext(Runnable runnable, Integer time);

  void apply();
  void endTurn();
}
