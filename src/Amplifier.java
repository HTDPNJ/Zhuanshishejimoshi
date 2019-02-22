public class Amplifier
{
    /*扩音器
    * 类与类之间的关系*/
    private  Voice voice;

    public Amplifier()
    {
    }

    public Amplifier(Voice voice)
    {
        this.voice = voice;
    }
    public void say(){
        System.out.println(voice.getVoice()*1000);
    }
}
