from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer

app = Flask(__name__)
model = SentenceTransformer('bert-base-nli-mean-tokens')

@app.route('/embed', methods=['POST'])
def embed():
    data = request.json
    text = data.get('text', '')
    if not text:
        return jsonify({'error': 'Missing text'}), 400
    
    # Calcola l'embedding usando il modello BERT
    embedding = model.encode([text])[0].tolist()
    return jsonify({'embedding': embedding})

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status":"ok"}), 200


if __name__ == '__main__':
    app.run(port=5000)