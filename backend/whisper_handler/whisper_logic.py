from faster_whisper import WhisperModel

model_size = "large-v3"
### AVAILABLE MODEL NAMES
# tiny.en, tiny, base.en, base, small.en, small, medium.en, medium, large-v1, large-v2, large-v3, large, distil-large-v2, distil-medium.en, distil-small.en, distil-large-v3
###
_model = WhisperModel(model_size, device="cuda", compute_type="float16")

def whisper_transcribe(file):
    global _model
    segments, info = _model.transcribe(file)
    text_fragments = []
    for segment in segments:
        text = "[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text)
        text_fragments.append(text)
    return text_fragments