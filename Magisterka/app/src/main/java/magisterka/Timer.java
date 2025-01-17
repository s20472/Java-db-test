package magisterka;

public class Timer 
{
    long startTime;
    
    public Timer()
    {
        startTime = 0;
    }

    public void Start()
    {
        startTime = System.currentTimeMillis();
    }

    public long End()
    {
        return System.currentTimeMillis()-startTime;
    }
}
