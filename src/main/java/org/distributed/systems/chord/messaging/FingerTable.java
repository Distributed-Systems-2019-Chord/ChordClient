package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.util.List;

public class FingerTable {

    public static class Get implements Command {

        private long hash;

        public Get(long hash) {
            this.hash = hash;
        }

        public long getHash() {
            return hash;
        }
    }


    public static class Reply implements Response {

        public final List<ChordNode> successors;
        public final ChordNode predecessor;

        public Reply(List<ChordNode> successors, ChordNode predecessor) {
            this.successors = successors;
            this.predecessor = predecessor;
        }
    }

    public static class GetPredecessor implements Command {

        private final long nodeId;

        public GetPredecessor(long nodeId) {
            this.nodeId = nodeId;
        }

        public long getId() {
            return nodeId;
        }
    }

    public static class GetPredecessorReply implements Response {

        private final ChordNode predecessor;

        public GetPredecessorReply(ChordNode predecessor) {
            this.predecessor = predecessor;
        }

        public ChordNode getChordNode() {
            return this.predecessor;
        }
    }

    public static class SetPredecessor implements Command {

        private final ChordNode predecessor;

        public SetPredecessor(ChordNode predecessor) {
            this.predecessor = predecessor;
        }

        public ChordNode getNode() {
            return predecessor;
        }
    }

    public static class GetSuccessor implements Command {

        private Long id;

        public GetSuccessor(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    public static class GetSuccessorReply implements Response {

        private final ChordNode successor;

        public GetSuccessorReply(ChordNode successor) {
            this.successor = successor;
        }

        public ChordNode getChordNode() {
            return this.successor;
        }
    }

    public static class GetClosestPrecedingFinger implements Command {
        private long id;

        public long getId() {
            return id;
        }

        public GetClosestPrecedingFinger(long id) {
            this.id = id;
        }
    }

    public static class GetClosestPrecedingFingerReply implements Response {

        private final ChordNode closest;

        public GetClosestPrecedingFingerReply(ChordNode closest) {
            this.closest = closest;
        }

        public ChordNode getClosestChordNode() {
            return this.closest;
        }
    }


    public static class FindSuccessorReply implements Response {
        private final ChordNode successor;

        public FindSuccessorReply(ChordNode successor) {
            this.successor = successor;
        }

        public ChordNode getChordNode() {
            return this.successor;
        }
    }

    public static class UpdateFinger implements Command {
        private final long nodeId;

        private final int fingerTableIndex;

        public UpdateFinger(long id, int fingerTableIndex) {
            this.nodeId = id;
            this.fingerTableIndex = fingerTableIndex;
        }

        public long getNodeId() {
            return nodeId;
        }

        public int getFingerTableIndex() {
            return fingerTableIndex;
        }
    }

    public static class GetFingerTableSuccessor implements Command {

        private long fingerTableIndex;

        private long successor;

        public GetFingerTableSuccessor(long fingerTableIndex, long succ) {
            this.fingerTableIndex = fingerTableIndex;
            this.successor = succ;
        }

        public long getFingerTableIndex() {
            return fingerTableIndex;
        }

        public long getSuccessor() {
            return successor;
        }
    }

    public static class GetFingerTableSuccessorReply implements Response {

        private long fingerTableIndex;

        private ChordNode successor;

        public GetFingerTableSuccessorReply(long beginFinger, ChordNode succ) {
            this.fingerTableIndex = beginFinger;
            this.successor = succ;
        }

        public long getFingerTableIndex() {
            return fingerTableIndex;
        }

        public ChordNode getSuccessor() {
            return successor;
        }
    }

    // HET BEGIN

    public static class FindSuccessor implements Command{

    }

    public static class FindPredecessor implements Command{

    }

    // MY
    public static class FindMySuccessor extends FindSuccessor{
        long id;
        public FindMySuccessor(long id){
            this.id = id;
        }
        public long getId(){
            return this.id;
        }
    }

    public static class FindMySuccessorReply {
        ChordNode successor;
        public FindMySuccessorReply(ChordNode succ){
            this.successor = succ;
        }
        public ChordNode getSuccessor(){
            return this.successor;
        }
    }

    public static class FindMyPredecessor extends FindPredecessor {
        long id;

        public FindMyPredecessor(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    public static class FindMyPredecessorReply implements Response{
        ChordNode node;

        public ChordNode getNode() {
            return node;
        }

        public FindMyPredecessorReply(ChordNode node) {
            this.node = node;
        }
    }

    //FINGER
    public static class FindFingerSuccessor extends FindSuccessor{
        long id;

        public FindFingerSuccessor(long id){
            this.id = id;
        }
        public long getId(){
            return this.id;
        }
    }
    public static class FindFingerSuccessorReply {
        ChordNode successor;
        public FindFingerSuccessorReply(ChordNode succ){
            this.successor = succ;
        }
        public ChordNode getSuccessor(){
            return this.successor;
        }
    }
    public static class FindFingerPredecessor extends FindPredecessor{
        long id;

        public FindFingerPredecessor(long id){
            this.id = id;
        }
        public long getId(){
            return this.id;
        }
    }

    public static class FindFingerPredecessorReply implements Response{
        ChordNode node;

        public ChordNode getNode() {
            return node;
        }

        public FindFingerPredecessorReply(ChordNode node) {
            this.node = node;
        }
    }
    public static class FindSomeSuccessor extends FindSuccessor{
        long id;

        public FindSomeSuccessor(long id){
            this.id = id;
        }
        public long getId(){
            return this.id;
        }
    }
    public static class FindSomeSuccessorReply {
        ChordNode successor;
        public FindSomeSuccessorReply(ChordNode succ){
            this.successor = succ;
        }
        public ChordNode getSuccessor(){
            return this.successor;
        }
    }
    public static class FindSomePredecessor extends FindPredecessor{
        long id;

        public FindSomePredecessor(long id){
            this.id = id;
        }
        public long getId(){
            return this.id;
        }
    }

    public static class FindSomePredecessorReply implements Response{
        ChordNode node;

        public ChordNode getNode() {
            return node;
        }

        public FindSomePredecessorReply(ChordNode node) {
            this.node = node;
        }
    }

}
