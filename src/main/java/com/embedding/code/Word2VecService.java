package com.embedding.code;

import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Word2VecService {

    private Word2Vec modelo;

    public boolean isModeloCargado() {
        return modelo != null;
    }

    public int getVocabSize() {
        return modelo != null ? modelo.getVocab().numWords() : 0;
    }

    // --- FASE 1: CARGA ---
    public void cargarModeloExterno(File file) throws Exception {
        this.modelo = WordVectorSerializer.readWord2VecModel(file);
    }

    // --- FASE 2: ENTRENAMIENTO (SEGÚN RÚBRICA PDF) ---
    public void entrenarModeloLocal(String textoCorpus) {
        // Limpieza básica (letras y minúsculas)
        String textoLimpio = textoCorpus.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", "").toLowerCase();

        List<String> oraciones = Arrays.asList(textoLimpio.split("\\n"));
        SentenceIterator iter = new CollectionSentenceIterator(oraciones);

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        // PARÁMETROS EXACTOS DEL PDF [cite: 104-110]
        this.modelo = new Word2Vec.Builder()
                .minWordFrequency(1)    //
                .layerSize(50)          //
                .seed(42)
                .windowSize(5)          //
                .epochs(10)             //
                .elementsLearningAlgorithm(new SkipGram<>()) //  sg=1
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        this.modelo.fit();
    }

    // --- CÁLCULOS ---
    public double calcularSimilitudPalabras(String p1, String p2) throws Exception {
        if (modelo == null) throw new Exception("Modelo no cargado");
        if (!modelo.hasWord(p1) || !modelo.hasWord(p2)) return -999;
        return modelo.similarity(p1, p2);
    }

    public double calcularSimilitudFrases(String f1, String f2) throws Exception {
        if (modelo == null) throw new Exception("Modelo no cargado");

        INDArray v1 = obtenerVectorPromedio(f1);
        INDArray v2 = obtenerVectorPromedio(f2);

        if (v1 == null || v2 == null) return -999;
        return Transforms.cosineSim(v1, v2);
    }

    private INDArray obtenerVectorPromedio(String frase) {
        String[] tokens = frase.toLowerCase().split("\\s+");
        List<String> validos = new ArrayList<>();

        for (String tok : tokens) {
            tok = tok.replaceAll("[^a-zñ]", "");
            if (tok.length() > 0 && modelo.hasWord(tok)) {
                validos.add(tok);
            }
        }

        if (validos.isEmpty()) return null;
        return modelo.getWordVectors(validos).mean(0);
    }
}