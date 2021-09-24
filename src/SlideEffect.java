public class SlideEffect {
    public enum ChangeAnimation {
        WipeLeft,
        WipeRight,
        Cross,
        WipeUp,
        WipeDown,
        None
    }

    private ChangeAnimation changeAnimation;
    private int duration;

    public SlideEffect(ChangeAnimation changeAnimation, int duration) {
        this.changeAnimation = changeAnimation;
        this.duration = duration;
    }

    public ChangeAnimation getChangeAnimation() {
        return changeAnimation;
    }

    public int getDuration() {
        return duration;
    }

    public void setChangeAnimation(ChangeAnimation changeAnimation) {
        this.changeAnimation = changeAnimation;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
