from faster_whisper import WhisperModel

model_size = "large-v3"
_model = WhisperModel(model_size, device="cuda", compute_type="float16")

def whisper_transcribe(file):
    global _model
    segments, info = _model.transcribe(file)
    text_fragments = []
    for segment in segments:
        text = "[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text)
        text_fragments.append(text)
    return text_fragments