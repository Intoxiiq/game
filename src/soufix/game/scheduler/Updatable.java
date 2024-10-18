package soufix.game.scheduler;

public abstract class Updatable implements IUpdatable
{

  private final long wait;
  private long lastTime=System.currentTimeMillis();

  public Updatable(int wait)
  {
    this.wait=wait;
  }

  protected boolean verify()
  {
    if(System.currentTimeMillis()-this.lastTime>this.wait)
    {
      this.lastTime=System.currentTimeMillis();
      return true;
    }
    return false;
  }

  public abstract void update();
}
