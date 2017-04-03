package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.output.FrameDeconstructor;
import com.sheffield.leapmotion.util.Serializer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 10/03/17.
 */
public class TestHandFactory {

    private String serialized = null;
    private String serializedNext = null;

    public static final Bone.Type[] fingerBoneTypes = { Bone.Type.TYPE_METACARPAL, Bone.Type.TYPE_PROXIMAL,
            Bone.Type.TYPE_INTERMEDIATE, Bone.Type.TYPE_DISTAL };
    public static final Finger.Type[] fingerTypes = { Finger.Type.TYPE_THUMB, Finger.Type.TYPE_INDEX, Finger.Type.TYPE_MIDDLE,
            Finger.Type.TYPE_RING, Finger.Type.TYPE_PINKY };

    private Controller ctrl;

    @Before
    public void setup(){

        Controller ctrl = new Controller();

        Properties.SINGLE_DATA_POOL = true;

        serialized = "[115,1,-11,27,8,-96,-49,3,16,-83,-120,-87,-17,4,26,-98,3,8,18,34,51,10,15,13,-111,-51,94,66,21,-38,-80,127,67,29,33,60,126,-63,18,15,13,46,-99,68,62,21,115,-94,4,63,29,120,95,85,-65,26,15,13,-1,-50,60,-63,21,-16,73,-99,64,29,-44,59,-93,-62,42,15,13,46,-73,61,62,21,92,104,90,-65,29,52,-83,-7,-66,50,33,10,15,13,35,-83,67,66,21,93,53,101,67,29,-97,16,90,-62,21,-94,-77,41,66,24,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,58,27,9,-92,-116,-5,117,10,76,72,-64,17,17,35,-100,99,-90,-92,91,64,25,71,-16,40,67,117,-119,54,-64,66,87,10,27,9,-86,70,-128,79,38,-2,-18,63,17,-87,-8,-78,16,-121,121,-73,63,25,36,-69,-109,-26,-7,-98,-51,63,18,27,9,-96,-58,-37,119,111,110,-73,-65,17,-7,46,-10,-2,17,-35,-17,63,25,-105,-39,-94,77,-84,-120,-121,-65,26,27,9,31,109,-90,-27,43,-95,-51,-65,17,25,-9,26,1,-16,-107,-124,-65,25,44,123,-12,53,19,33,-17,63,73,19,52,-28,-62,17,-10,-31,63,85,-31,-26,35,67,90,15,13,9,-82,10,66,21,4,-77,118,67,29,18,39,-80,66,104,0,-123,1,30,49,89,63,-115,1,0,0,0,0,-107,1,-38,-72,31,63,-99,1,-10,8,-85,66,-94,1,15,13,58,4,-121,66,21,-80,123,-88,66,29,-6,11,107,67,-83,1,-17,-116,111,66,-78,1,15,13,-10,110,80,66,21,-99,75,105,67,29,-8,5,-14,65,-70,1,87,10,27,9,17,93,30,89,-116,62,-17,63,17,-48,49,36,102,-8,-9,-55,63,25,-68,-12,-97,66,92,1,-77,63,18,27,9,-53,-17,-54,-89,10,-118,-54,-65,17,-89,-50,67,80,49,18,-23,63,25,63,118,-65,-60,8,-65,-30,63,26,27,9,103,117,-110,99,79,18,-81,63,17,57,-46,-46,-112,-51,-53,-30,-65,25,-88,58,53,-8,56,-45,-23,63,34,-89,4,8,-75,1,16,18,26,51,10,15,13,-81,53,88,66,21,48,23,-124,67,29,46,63,-31,-62,18,15,13,-95,110,115,62,21,24,14,40,-66,29,4,22,117,-65,26,15,13,-111,-7,-124,-63,21,-46,124,59,65,29,-11,16,-94,-62,37,-125,111,-111,65,45,24,21,93,66,56,0,64,2,77,5,61,-49,-66,90,15,13,-122,76,11,66,21,94,-122,-128,67,29,15,-16,40,-63,117,-31,-26,35,67,120,1,-126,1,15,13,63,28,76,66,21,61,-63,-122,67,29,-62,-1,-54,-62,-118,1,15,13,69,-126,54,66,21,113,-98,-120,67,29,-85,-128,-97,-62,-110,1,15,13,32,-93,20,66,21,85,21,-122,67,29,32,100,34,-62,-102,1,15,13,23,-122,5,66,21,-62,-80,114,67,29,106,-110,-68,65,-94,1,15,13,70,-31,91,66,21,-66,-53,-125,67,29,103,-53,-25,-62,-86,1,87,10,27,9,-18,115,124,-83,8,111,-17,63,17,42,-95,56,-26,33,-50,-62,63,25,-96,50,3,119,29,-67,-67,63,18,27,9,40,10,-124,-103,58,-18,-58,-65,17,-59,-32,-106,99,-5,95,-19,63,25,-46,80,-109,127,71,-90,-42,63,26,27,9,-60,6,-105,65,78,-6,-85,-65,17,-84,-59,28,-60,-94,-108,-41,-65,25,-124,103,95,-35,-125,-78,-19,63,-86,1,87,10,27,9,2,108,-96,-67,-46,4,-17,63,17,-122,38,104,56,-2,106,-70,63,25,24,-16,114,33,-111,-117,-52,63,18,27,9,-76,-92,89,111,-83,100,-64,-65,17,-21,-83,13,-68,12,-109,-17,63,25,-43,108,23,15,18,-96,-71,63,26,27,9,87,-105,114,79,-27,-41,-54,-65,17,26,41,-96,-4,92,19,-64,-65,25,63,32,11,-115,123,7,-17,63,-86,1,87,10,27,9,4,108,-96,-67,-46,4,-17,63,17,-119,38,104,56,-2,106,-70,63,25,26,-16,114,33,-111,-117,-52,63,18,27,9,11,-49,-48,49,44,-40,-81,-65,17,55,-5,68,-110,-107,100,-17,63,25,-84,-113,72,-84,-66,-128,-57,-65,26,27,9,123,-113,-93,45,-44,109,-50,-65,17,-116,77,95,-11,-62,1,-59,63,25,-1,-34,111,112,-64,-94,-18,63,-86,1,87,10,27,9,3,108,-96,-67,-46,4,-17,63,17,-120,38,104,56,-2,106,-70,63,25,24,-16,114,33,-111,-117,-52,63,18,27,9,72,29,-82,-98,-106,44,-123,-65,17,98,12,89,-91,84,-112,-19,63,25,103,-69,-66,-95,8,124,-40,-65,26,27,9,17,-69,46,46,-28,108,-49,-65,17,87,104,-46,97,18,-106,-41,63,25,103,-94,68,64,4,-79,-20,63,56,-128,-41,3,74,-89,4,8,-76,1,16,18,26,51,10,15,13,32,-78,87,66,21,-128,15,100,67,29,-14,-74,-115,-62,18,15,13,-37,12,-111,62,21,22,-104,-112,61,29,-126,-40,116,-65,26,15,13,116,21,75,-63,21,-38,-18,28,65,29,-89,24,-93,-62,37,-74,65,-104,65,45,116,-19,67,66,56,0,64,2,77,-120,56,-50,-66,90,15,13,-36,110,19,66,21,-41,-124,95,67,29,-67,-106,-8,65,117,-31,-26,35,67,120,0,-126,1,15,13,2,-76,69,66,21,55,6,99,67,29,69,-121,90,-62,-118,1,15,13,-63,99,33,66,21,7,-61,96,67,29,-6,-35,-65,-63,-110,1,15,13,-33,100,-36,65,21,39,87,93,67,29,-105,46,-88,65,-102,1,15,13,-33,100,-36,65,21,39,87,93,67,29,-105,46,-88,65,-94,1,15,13,-4,78,91,66,21,89,-62,100,67,29,-109,-60,-105,-62,-86,1,87,10,27,9,-118,21,126,-29,-69,-100,-48,63,17,27,-50,106,-54,-58,111,-20,63,25,54,-34,31,25,72,50,-40,63,18,27,9,-89,96,93,114,118,-34,-19,-65,17,60,-5,52,-103,-74,-79,-64,63,25,-76,104,-109,9,-42,100,-43,63,26,27,9,20,63,-114,-51,41,-74,-49,63,17,68,24,-96,-43,97,35,-36,-65,25,39,17,-38,-47,80,-96,-21,63,-86,1,87,10,27,9,-35,-113,88,13,-11,52,-57,63,17,108,46,-82,-80,38,54,-17,63,25,125,-8,-66,-21,13,23,-64,63,18,27,9,81,-86,4,-89,22,60,-18,-65,17,101,-79,-106,60,-42,-90,-54,63,25,-50,48,33,49,-82,46,-48,-65,26,27,9,11,-125,95,-22,110,117,-47,-65,17,127,123,93,91,82,-85,-78,-65,25,64,-101,82,57,-96,-78,-18,63,-86,1,87,10,27,9,-35,-113,88,13,-11,52,-57,63,17,107,46,-82,-80,38,54,-17,63,25,124,-8,-66,-21,13,23,-64,63,18,27,9,-72,102,-32,-108,-74,34,-18,-65,17,119,-70,14,-39,8,-63,-54,63,25,114,-7,89,-26,-125,-35,-48,-65,26,27,9,-55,-84,-77,83,-101,33,-46,-65,17,120,-56,-90,-56,2,19,-78,-65,25,-50,-37,-89,50,16,-101,-18,63,-86,1,87,10,27,9,-35,-113,88,13,-11,52,-57,63,17,107,46,-82,-80,38,54,-17,63,25,125,-8,-66,-21,13,23,-64,63,18,27,9,78,40,34,35,11,121,-18,-65,17,40,-64,61,29,111,95,-54,63,25,-106,87,-107,-59,-86,-45,-52,-65,26,27,9,113,-56,-4,97,-125,110,-49,-65,17,119,108,108,-20,7,49,-76,-65,25,19,28,69,-77,-41,-22,-18,63,74,-89,4,8,-74,1,16,18,26,51,10,15,13,59,49,90,66,21,-30,-26,86,67,29,112,99,53,-62,18,15,13,92,109,49,-66,21,115,56,99,-65,29,-54,-120,-38,62,26,15,13,116,-22,15,-65,21,59,89,-121,64,29,-38,121,-96,-62,37,74,-42,-114,65,45,-40,-25,123,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,1,94,2,66,21,39,-93,76,67,29,-105,108,-116,66,117,-31,-26,35,67,120,0,-126,1,15,13,-67,-27,103,66,21,15,-4,91,67,29,-44,-76,105,-62,-118,1,15,13,-83,107,122,66,21,-56,-76,115,67,29,-3,-86,-117,-62,-110,1,15,13,43,2,99,66,21,1,-101,-121,67,29,-80,68,9,-62,-102,1,15,13,29,123,49,66,21,-121,11,118,67,29,95,-95,-57,65,-94,1,15,13,22,19,87,66,21,-42,-81,85,67,29,12,-30,41,-62,-86,1,87,10,27,9,47,91,107,30,15,108,-17,63,17,33,-114,33,1,90,24,-80,-65,25,8,31,-91,22,-2,-42,-58,63,18,27,9,-44,-41,-37,67,25,-35,-121,-65,17,32,-94,19,-31,25,123,-19,63,25,44,-70,-125,44,-2,-32,-40,63,26,27,9,124,-108,108,-111,-114,43,-56,-65,17,-105,91,-6,0,10,-112,-40,-65,25,106,45,83,28,-50,-20,-20,63,-86,1,87,10,27,9,-110,26,120,-115,56,20,-17,63,17,35,-82,84,-73,85,2,-75,-65,25,-41,-115,-126,-25,35,-97,-52,63,18,27,9,-60,-108,60,47,4,-99,-55,63,17,-86,-109,113,-13,113,74,-23,63,25,40,-63,-90,-34,-62,-121,-30,-65,26,27,9,-48,2,17,-5,-80,-119,-64,-65,17,38,28,48,-86,-58,109,-29,63,25,-90,38,125,-26,-96,22,-23,63,-86,1,87,10,27,9,-110,26,120,-115,56,20,-17,63,17,26,-82,84,-73,85,2,-75,-65,25,-30,-115,-126,-25,35,-97,-52,63,18,27,9,-50,-2,96,-42,-98,-21,-60,63,17,121,-89,-127,-76,-101,2,-35,-65,25,119,115,-6,73,69,10,-20,-65,26,27,9,-117,-27,-111,124,-85,45,-58,63,17,114,125,6,108,14,103,-20,63,25,-92,-22,-7,76,25,81,-37,-65,-86,1,87,10,27,9,-110,26,120,-115,56,20,-17,63,17,5,-82,84,-73,85,2,-75,-65,25,-35,-115,-126,-25,35,-97,-52,63,18,27,9,48,33,127,28,-21,-25,118,63,17,-102,103,112,36,-18,-57,-19,-65,25,-34,88,16,-21,37,106,-41,-65,26,27,9,-4,104,119,-64,-30,122,-50,63,17,-41,57,117,76,29,-46,-42,63,25,3,-8,35,-46,-67,-24,-20,-65,74,-89,4,8,-73,1,16,18,26,51,10,15,13,29,117,-122,66,21,42,-125,88,67,29,-98,-50,9,-62,18,15,13,-36,36,-89,-66,21,60,49,93,-65,29,-103,56,-60,62,26,15,13,-52,-86,-110,62,21,4,-4,13,65,29,-13,100,-93,-62,37,37,-21,-121,65,45,-55,54,114,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,124,83,47,66,21,-32,8,79,67,29,-85,112,-95,66,117,-31,-26,35,67,120,0,-126,1,15,13,124,54,-111,66,21,-68,82,93,67,29,102,70,59,-62,-118,1,15,13,58,54,-94,66,21,-84,-47,115,67,29,-5,47,99,-62,-110,1,15,13,125,-60,-106,66,21,-63,-88,-122,67,29,86,107,-65,-63,-102,1,15,13,-17,38,95,66,21,-17,101,118,67,29,100,22,-43,65,-94,1,15,13,-125,3,-124,66,21,41,83,87,67,29,-127,-120,-3,-63,-86,1,87,10,27,9,127,76,45,88,-26,32,-18,63,17,-68,-98,-54,69,57,14,-56,-65,25,-15,-109,-88,-6,125,-26,-47,63,18,27,9,34,-76,-31,104,-49,-29,-86,63,17,106,59,-56,110,-14,-38,-20,63,25,105,-31,78,-65,112,118,-37,63,26,27,9,-24,85,-117,116,106,77,-43,-65,17,-110,124,32,124,-102,-22,-40,-65,25,-81,35,-29,44,-55,123,-21,63,-86,1,87,10,27,9,84,-93,-86,-125,-53,-110,-19,63,17,96,117,-107,55,-40,116,-54,-65,25,-40,-104,-119,-90,16,-113,-44,63,18,27,9,0,-38,31,70,-91,-42,-42,63,17,-33,-76,31,111,-100,-116,-24,63,25,-91,-68,8,105,-117,14,-31,-65,26,27,9,68,-86,-87,-114,44,113,-63,-65,17,-17,-23,-42,-59,125,110,-29,63,25,-33,-114,-122,-59,68,12,-23,63,-86,1,87,10,27,9,84,-93,-86,-125,-53,-110,-19,63,17,106,117,-107,55,-40,116,-54,-65,25,-43,-104,-119,-90,16,-113,-44,63,18,27,9,-62,-34,-51,-78,67,99,-55,63,17,104,27,-3,112,18,97,-35,-65,25,-120,-120,-85,-64,42,-74,-21,-65,26,27,9,114,-112,-33,119,-101,-28,-44,63,17,-107,113,-48,-126,39,-90,-21,63,25,-46,-70,29,39,19,-121,-40,-65,-86,1,87,10,27,9,85,-93,-86,-125,-53,-110,-19,63,17,114,117,-107,55,-40,116,-54,-65,25,-41,-104,-119,-90,16,-113,-44,63,18,27,9,97,-69,43,-84,-93,125,-79,-65,17,48,-124,-114,29,-60,86,-19,-65,25,32,-38,30,-57,-42,44,-39,-65,26,27,9,18,73,-97,92,124,13,-40,63,17,-22,14,77,57,-120,-36,-43,63,25,-85,-13,-112,-59,-25,-112,-21,-65,74,-89,4,8,-72,1,16,18,26,51,10,15,13,5,72,-101,66,21,-110,67,98,67,29,40,-12,-31,-63,18,15,13,-104,-91,30,-65,21,122,87,66,-65,29,-79,-26,75,62,26,15,13,-1,-120,-111,-65,21,-89,60,61,65,29,-40,-119,-91,-62,37,-120,119,113,65,45,44,-28,61,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,85,59,89,66,21,125,-69,89,67,29,-124,108,-88,66,117,-31,-26,35,67,120,0,-126,1,15,13,98,67,-84,66,21,3,-41,101,67,29,-46,-14,22,-62,-118,1,15,13,5,12,-61,66,21,125,-53,115,67,29,6,-105,37,-62,-110,1,15,13,-61,84,-72,66,21,104,126,-125,67,29,-85,-45,108,-63,-102,1,15,13,-47,-36,-122,66,21,-37,-15,111,67,29,-84,-114,-33,65,-94,1,15,13,17,69,-105,66,21,-48,106,97,67,29,93,-67,-47,-63,-86,1,87,10,27,9,25,77,-114,5,-89,-1,-21,63,17,-28,121,48,-28,34,51,-40,-65,25,-84,123,116,22,71,91,-45,63,18,27,9,-30,-43,3,61,39,-104,-59,63,17,-77,38,36,-13,-108,90,-22,63,25,103,-8,-76,34,126,84,-31,63,26,27,9,16,-20,-46,-69,4,12,-35,-65,17,92,4,-96,95,-128,15,-37,-65,25,-7,25,99,-4,-116,25,-23,63,-86,1,87,10,27,9,40,-96,-27,-3,97,-128,-25,63,17,-28,-90,-92,-1,-78,44,-34,-65,25,36,-27,-97,118,-112,62,-33,63,18,27,9,-49,-56,-67,48,-76,24,-27,63,17,8,-89,50,-92,-88,83,-27,63,25,3,82,-108,-82,-47,71,-42,-65,26,27,9,24,-29,49,-123,7,-93,-60,-65,17,67,-16,50,-16,23,123,-30,63,25,-113,47,23,-101,1,-100,-23,63,-86,1,87,10,27,9,40,-96,-27,-3,97,-128,-25,63,17,-25,-90,-92,-1,-78,44,-34,-65,25,26,-27,-97,118,-112,62,-33,63,18,27,9,19,46,51,-24,-19,-75,-47,63,17,-106,111,61,-95,-48,-72,-36,-65,25,-31,38,-104,-83,-33,48,-21,-65,26,27,9,-127,102,85,-10,-78,-44,-29,63,17,63,-93,-19,65,-17,74,-24,63,25,71,39,-62,18,-42,124,-55,-65,-86,1,87,10,27,9,38,-96,-27,-3,97,-128,-25,63,17,-18,-90,-92,-1,-78,44,-34,-65,25,28,-27,-97,118,-112,62,-33,63,18,27,9,24,20,-92,-51,-102,-38,-55,-65,17,70,-89,-120,54,118,-43,-22,-65,25,12,-37,4,80,14,49,-32,-65,26,27,9,-5,-47,107,87,-18,-69,-28,63,17,30,-89,48,-95,-37,120,-47,63,25,75,-125,-120,-58,54,-63,-26,-65,106,15,13,47,-79,81,64,21,25,-32,-14,62,29,-51,-102,-104,67,-94,1,27,9,-92,-116,-5,117,10,76,72,-64,17,17,35,-100,99,-90,-92,91,64,25,71,-16,40,67,117,-119,54,-64,-86,1,87,10,27,9,-86,70,-128,79,38,-2,-18,63,17,-87,-8,-78,16,-121,121,-73,63,25,36,-69,-109,-26,-7,-98,-51,63,18,27,9,-96,-58,-37,119,111,110,-73,-65,17,-7,46,-10,-2,17,-35,-17,63,25,-105,-39,-94,77,-84,-120,-121,-65,26,27,9,31,109,-90,-27,43,-95,-51,-65,17,25,-9,26,1,-16,-107,-124,-65,25,44,123,-12,53,19,33,-17,63,-79,1,19,52,-28,-62,17,-10,-31,63,-6,1,15,13,0,0,0,0,21,-84,16,40,67,29,0,0,0,0,-126,2,15,13,36,-81,69,67,21,36,-81,69,67,29,103,81,-8,66,-107,2,36,111,-26,66,-78,2,-77,1,8,-110,-124,-8,21,18,13,76,80,55,56,55,56,55,57,49,51,53,51,54,29,7,114,19,64,37,-52,116,0,64,45,0,0,-21,67,50,15,13,0,0,0,0,21,0,0,0,0,29,0,0,0,0,72,1,80,0,88,1,96,0,104,7,112,1,120,1,-128,1,1,-118,1,5,49,46,55,46,48,-107,1,35,-123,-26,66,-102,1,87,10,27,9,0,0,0,0,0,0,-16,63,17,0,0,0,0,0,0,0,0,25,0,0,0,0,0,0,0,0,18,27,9,0,0,0,0,0,0,0,0,17,0,0,0,0,0,0,-16,63,25,0,0,0,0,0,0,0,0,26,27,9,0,0,0,0,0,0,0,0,17,0,0,0,0,0,0,0,0,25,0,0,0,0,0,0,-16,63,-91,1]";

        serializedNext = "[115,1,-11,27,8,-95,-49,3,16,-37,-53,-87,-17,4,26,-98,3,8,18,34,51,10,15,13,-69,73,94,66,21,-18,-114,127,67,29,-17,-32,-124,-63,18,15,13,-87,92,67,62,21,20,108,4,63,29,-99,-109,85,-65,26,15,13,17,19,74,-63,21,-121,-89,-24,-66,29,111,62,-92,-62,42,15,13,-98,62,61,62,21,20,-121,90,-65,29,-127,88,-7,-66,50,33,10,15,13,27,-31,66,66,21,106,11,101,67,29,11,-42,92,-62,21,-45,-61,41,66,24,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,58,27,9,124,1,-63,-119,-20,93,72,-64,17,-15,-122,51,-96,93,-96,91,64,25,50,35,17,72,97,61,55,-64,66,87,10,27,9,14,-87,56,-6,95,-3,-18,63,17,31,-40,-1,95,-36,-1,-73,63,25,-103,52,122,47,5,-111,-51,63,18,27,9,-42,14,36,48,-21,-20,-73,-65,17,71,-47,117,-41,-121,-37,-17,63,25,100,73,89,46,-59,5,-119,-65,26,27,9,-104,15,107,116,-37,-108,-51,-65,17,-47,-102,120,-108,-69,-6,-125,-65,25,-31,25,-38,-25,-44,33,-17,63,73,-40,30,47,46,24,-6,-31,63,85,22,-23,35,67,90,15,13,19,-82,10,66,21,5,-77,118,67,29,-6,38,-80,66,104,0,-123,1,-10,-12,88,63,-115,1,0,0,0,0,-107,1,-78,95,33,63,-99,1,-10,8,-85,66,-94,1,15,13,-98,85,-120,66,21,36,43,-87,66,29,36,-100,106,67,-83,1,-17,-116,111,66,-78,1,15,13,2,47,80,66,21,-39,53,105,67,29,-79,124,-20,65,-70,1,87,10,27,9,-43,-65,-123,59,75,62,-17,63,17,-30,120,-24,49,-111,32,-54,63,25,63,104,-128,26,116,57,-78,63,18,27,9,67,17,53,95,-29,114,-54,-65,17,33,14,127,124,-29,25,-23,63,25,24,43,-74,9,-58,-74,-30,63,26,27,9,-67,48,-66,8,120,67,-80,63,17,-116,31,82,-36,-2,-67,-30,-65,25,-34,39,-29,-58,117,-37,-23,63,34,-89,4,8,-75,1,16,18,26,51,10,15,13,-99,46,87,66,21,5,3,-124,67,29,-71,-83,-30,-62,18,15,13,110,81,114,62,21,-39,-86,40,-66,29,-14,32,117,-65,26,15,13,-123,-79,-96,-63,21,-13,-110,109,64,29,87,-5,-94,-62,37,-125,111,-111,65,45,24,21,93,66,56,0,64,2,77,96,-128,-50,-66,90,15,13,-108,76,11,66,21,95,-122,-128,67,29,53,-15,40,-63,117,22,-23,35,67,120,1,-126,1,15,13,15,34,75,66,21,-111,-81,-122,67,29,6,112,-52,-62,-118,1,15,13,100,-95,53,66,21,-125,-114,-120,67,29,-1,-18,-96,-62,-110,1,15,13,-39,-6,19,66,21,-117,0,-122,67,29,-94,57,37,-62,-102,1,15,13,-17,58,5,66,21,38,-104,114,67,29,11,40,-73,65,-94,1,15,13,89,-41,90,66,21,120,-74,-125,67,29,-30,55,-23,-62,-86,1,87,10,27,9,-123,39,-59,-107,6,112,-17,63,17,-15,127,100,-121,45,-39,-62,63,25,-63,13,51,-32,113,93,-67,63,18,27,9,-116,127,-62,75,116,-27,-58,-65,17,88,-57,-31,-41,-81,98,-19,63,25,66,45,-123,-99,115,-102,-42,63,26,27,9,120,-70,52,87,-20,77,-85,-65,17,116,-78,-107,23,-18,-124,-41,-65,25,89,-113,76,44,65,-74,-19,63,-86,1,87,10,27,9,7,-48,-40,79,-58,6,-17,63,17,44,-12,38,-79,9,-125,-70,63,25,-7,65,24,13,-12,99,-52,63,18,27,9,72,-97,104,-66,-57,114,-64,-65,17,93,-16,21,44,-64,-111,-17,63,25,-124,-1,-100,80,6,-30,-71,63,26,27,9,-12,-71,60,-79,9,-85,-54,-65,17,-88,-125,-47,71,44,50,-64,-65,25,-56,73,-106,12,-26,8,-17,63,-86,1,87,10,27,9,9,-48,-40,79,-58,6,-17,63,17,45,-12,38,-79,9,-125,-70,63,25,-5,65,24,13,-12,99,-52,63,18,27,9,-84,46,10,117,-116,8,-80,-65,17,-95,-46,103,76,114,99,-17,63,25,97,-91,60,-78,48,-108,-57,-65,26,27,9,19,18,35,-69,45,74,-50,-65,17,-117,-32,120,33,91,21,-59,63,25,-102,84,-3,76,30,-92,-18,63,-86,1,87,10,27,9,8,-48,-40,79,-58,6,-17,63,17,45,-12,38,-79,9,-125,-70,63,25,-6,65,24,13,-12,99,-52,63,18,27,9,-128,32,-42,-77,-48,-35,-123,-65,17,42,83,49,57,64,-118,-19,63,25,-69,127,126,-118,42,-103,-40,-65,26,27,9,-125,18,120,117,-120,77,-49,-65,17,-90,-29,-3,-105,-55,-78,-41,63,25,-82,78,-75,22,62,-83,-20,63,56,-127,-41,3,74,-89,4,8,-76,1,16,18,26,51,10,15,13,-60,-56,86,66,21,-37,-15,99,67,29,-50,26,-113,-62,18,15,13,-28,-48,-113,62,21,-68,33,-111,61,29,-48,5,117,-65,26,15,13,9,98,-127,-63,21,-20,-60,-117,64,29,75,65,-94,-62,37,-74,65,-104,65,45,116,-19,67,66,56,0,64,2,77,107,102,-51,-66,90,15,13,-33,110,19,66,21,-41,-124,95,67,29,-107,-106,-8,65,117,22,-23,35,67,120,0,-126,1,15,13,-24,-31,68,66,21,-85,-18,98,67,29,-102,68,93,-62,-118,1,15,13,-63,-32,32,66,21,83,-87,96,67,29,68,43,-59,-63,-110,1,15,13,75,-47,-37,65,21,-119,61,93,67,29,-48,1,-93,65,-102,1,15,13,75,-47,-37,65,21,-119,61,93,67,29,-48,1,-93,65,-94,1,15,13,122,101,90,66,21,-105,-86,100,67,29,62,38,-103,-62,-86,1,87,10,27,9,-12,-107,7,80,-65,-95,-48,63,17,116,-117,83,78,122,115,-20,63,25,-43,-113,-121,13,102,29,-40,63,18,27,9,40,72,-17,44,13,-37,-19,-65,17,92,112,91,-61,114,-74,-64,63,25,0,-62,-10,-115,-19,118,-43,63,26,27,9,21,53,49,60,-14,-34,-49,63,17,-45,-115,88,87,-78,19,-36,-65,25,14,-101,-100,66,95,-95,-21,63,-86,1,87,10,27,9,28,-28,14,-87,5,63,-57,63,17,102,-122,-24,-67,-30,53,-17,63,25,68,-68,15,35,-62,16,-64,63,18,27,9,-75,-45,-39,79,106,62,-18,-65,17,-124,-71,-125,7,-28,-85,-54,63,25,-78,30,41,46,41,27,-48,-65,26,27,9,4,57,-9,56,-20,97,-47,-65,17,64,46,-93,-59,-39,-86,-78,-65,25,55,22,28,-38,101,-75,-18,63,-86,1,87,10,27,9,28,-28,14,-87,5,63,-57,63,17,103,-122,-24,-67,-30,53,-17,63,25,67,-68,15,35,-62,16,-64,63,18,27,9,-6,-82,-103,79,36,40,-18,-65,17,-84,116,-48,-65,19,-61,-54,63,25,11,72,59,30,-75,-75,-48,-65,26,27,9,68,-109,117,-120,28,-6,-47,-65,17,91,45,-67,125,55,36,-78,-65,25,104,54,-125,7,-70,-96,-18,63,-86,1,87,10,27,9,26,-28,14,-87,5,63,-57,63,17,102,-122,-24,-67,-30,53,-17,63,25,66,-68,15,35,-62,16,-64,63,18,27,9,-79,-88,60,46,-63,122,-18,-65,17,-37,7,-32,-85,-20,100,-54,63,25,-37,-1,32,-118,-98,-79,-52,-65,26,27,9,-122,7,97,80,117,76,-49,-65,17,72,-53,59,103,-105,46,-76,-65,25,33,72,33,-60,6,-19,-18,63,74,-89,4,8,-74,1,16,18,26,51,10,15,13,-47,-109,89,66,21,5,-64,86,67,29,35,42,56,-62,18,15,13,104,-101,48,-66,21,-28,30,99,-65,29,101,29,-37,62,26,15,13,-122,59,-89,-64,21,6,-36,-59,-65,29,-50,-4,-97,-62,37,74,-42,-114,65,45,-40,-25,123,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,8,94,2,66,21,40,-93,76,67,29,-125,108,-116,66,117,22,-23,35,67,120,0,-126,1,15,13,-39,50,103,66,21,121,-47,91,67,29,-34,123,108,-62,-118,1,15,13,-33,-94,121,66,21,-121,-121,115,67,29,6,30,-115,-62,-110,1,15,13,-68,97,98,66,21,-101,-121,-121,67,29,36,56,12,-62,-102,1,15,13,18,49,49,66,21,-24,-13,117,67,29,-29,17,-62,65,-94,1,15,13,19,120,86,66,21,37,-118,85,67,29,26,-101,44,-62,-86,1,87,10,27,9,-90,-7,-8,-41,27,110,-17,63,17,-2,120,-14,100,-5,-5,-81,-65,25,-53,-114,121,54,91,-82,-58,63,18,27,9,-56,-85,-105,-45,-29,56,-121,-65,17,16,48,29,-40,103,126,-19,63,25,3,68,-70,-128,117,-47,-40,63,26,27,9,56,12,-37,-62,101,1,-56,-65,17,28,-78,45,-71,58,-127,-40,-65,25,115,-118,-79,73,35,-14,-20,63,-86,1,87,10,27,9,-19,75,-51,66,-31,22,-17,63,17,71,98,45,65,-7,-29,-76,-65,25,-67,3,-40,-63,96,118,-52,63,18,27,9,20,-114,18,-58,-90,123,-55,63,17,-112,-38,-71,-35,84,71,-23,63,25,93,-124,-17,36,-31,-114,-30,-65,26,27,9,-47,-99,47,81,44,109,-64,-65,17,54,4,8,105,86,114,-29,63,25,3,-52,125,-46,67,20,-23,63,-86,1,87,10,27,9,-19,75,-51,66,-31,22,-17,63,17,66,98,45,65,-7,-29,-76,-65,25,-60,3,-40,-63,96,118,-52,63,18,27,9,122,61,-15,-20,11,-56,-60,63,17,47,-11,-121,-63,121,16,-35,-65,25,63,103,-74,101,85,8,-20,-65,26,27,9,75,19,-115,1,109,19,-58,63,17,11,83,113,121,-36,99,-20,63,25,54,74,59,-103,-84,99,-37,-65,-86,1,87,10,27,9,-19,75,-51,66,-31,22,-17,63,17,48,98,45,65,-7,-29,-76,-65,25,-64,3,-40,-63,96,118,-52,63,18,27,9,-32,-67,-108,-51,23,-113,117,63,17,30,90,117,-47,-89,-53,-19,-65,25,106,117,-127,-88,61,87,-41,-65,26,27,9,-27,-121,-43,-74,-97,79,-50,63,17,48,1,112,-7,96,-64,-42,63,25,-56,45,-7,-110,19,-17,-20,-65,74,-89,4,8,-73,1,16,18,26,51,10,15,13,-74,44,-122,66,21,71,94,88,67,29,95,-102,12,-62,18,15,13,13,-54,-90,-66,21,-2,19,93,-65,29,65,9,-59,62,26,15,13,-103,78,-118,-64,21,-36,89,2,64,29,-104,44,-94,-62,37,37,-21,-121,65,45,-55,54,114,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,-111,83,47,66,21,-30,8,79,67,29,119,112,-95,66,117,22,-23,35,67,120,0,-126,1,15,13,-31,-31,-112,66,21,124,41,93,67,29,69,17,62,-62,-118,1,15,13,98,-40,-95,66,21,115,-91,115,67,29,75,37,102,-62,-110,1,15,13,-116,123,-106,66,21,69,-105,-122,67,29,-12,-128,-59,-63,-102,1,15,13,22,-33,94,66,21,103,79,118,67,29,14,103,-49,65,-94,1,15,13,-96,-70,-125,66,21,-29,46,87,67,29,12,125,1,-62,-86,1,87,10,27,9,-30,83,6,101,71,36,-18,63,17,-79,-119,88,-48,-36,-3,-57,-65,25,-88,87,-76,-76,49,-43,-47,63,18,27,9,-12,106,5,-105,116,20,-85,63,17,55,-62,46,-128,-74,-34,-20,63,25,44,-47,-17,-82,-41,101,-37,63,26,27,9,41,-19,78,-116,75,57,-43,-65,17,-112,-65,-110,100,21,-35,-40,-65,25,-126,-107,82,48,-68,-126,-21,63,-86,1,87,10,27,9,84,108,-46,126,-36,-106,-19,63,17,-77,40,-74,-79,96,98,-54,-65,25,109,61,-123,-69,-112,125,-44,63,18,27,9,32,-55,-75,-95,-98,-57,-42,63,17,54,-77,-47,-37,67,-120,-24,63,25,-21,44,40,103,-50,25,-31,-65,26,27,9,118,57,-125,-26,80,81,-63,-65,17,41,43,81,86,-117,117,-29,63,25,-2,-23,42,105,44,8,-23,63,-86,1,87,10,27,9,83,108,-46,126,-36,-106,-19,63,17,-91,40,-74,-79,96,98,-54,-65,25,114,61,-123,-69,-112,125,-44,63,18,27,9,64,-119,-12,-28,-86,60,-55,63,17,-14,12,94,-123,-11,114,-35,-65,25,-95,86,45,-98,-97,-77,-21,-65,26,27,9,26,44,47,-103,65,-39,-44,63,17,-32,-47,50,-62,127,-94,-21,63,25,-93,45,-37,45,40,-95,-40,-65,-86,1,87,10,27,9,84,108,-46,126,-36,-106,-19,63,17,-102,40,-74,-79,96,98,-54,-65,25,112,61,-123,-69,-112,125,-44,63,18,27,9,4,127,-9,-110,-121,-102,-79,-65,17,19,-49,78,0,45,91,-19,-65,25,60,64,-66,45,-5,22,-39,-65,26,27,9,-17,-119,-9,-97,32,-8,-41,63,17,73,-61,-55,60,103,-54,-43,63,25,-26,-16,37,-41,35,-103,-21,-65,74,-89,4,8,-72,1,16,18,26,51,10,15,13,96,4,-101,66,21,46,33,98,67,29,-122,-65,-25,-63,18,15,13,65,-117,30,-65,21,-118,78,66,-65,29,-64,-76,77,62,26,15,13,-6,-92,-95,-64,21,42,51,-110,64,29,-78,-87,-91,-62,37,-120,119,113,65,45,44,-28,61,66,56,0,64,0,77,-85,-86,-86,62,90,15,13,120,59,89,66,21,-128,-69,89,67,29,47,108,-88,66,117,22,-23,35,67,120,0,-126,1,15,13,34,-10,-85,66,21,-42,-79,101,67,29,-115,-32,25,-62,-118,1,15,13,-3,-70,-62,66,21,-84,-91,115,67,29,-17,-91,40,-62,-110,1,15,13,-66,18,-72,66,21,-98,110,-125,67,29,62,63,121,-63,-102,1,15,13,-125,-70,-122,66,21,85,-36,111,67,29,113,-51,-39,65,-94,1,15,13,-38,0,-105,66,21,58,73,97,67,29,-13,108,-41,-63,-86,1,87,10,27,9,5,-17,28,-60,-97,3,-20,63,17,-78,23,-31,-73,-51,42,-40,-65,25,67,23,-126,-70,-81,78,-45,63,18,27,9,10,37,74,-90,-58,-87,-59,63,17,-67,46,26,57,-123,95,-22,63,25,93,-60,105,-87,-104,75,-31,63,26,27,9,80,97,-45,-87,101,-7,-36,-65,17,113,-118,-24,88,-79,3,-37,-65,25,-99,-25,100,21,27,34,-23,63,-86,1,87,10,27,9,-66,42,74,-75,-85,-122,-25,63,17,119,6,35,23,-110,33,-34,-65,25,-126,49,50,9,95,54,-33,63,18,27,9,51,-31,-3,-100,116,19,-27,63,17,-32,-100,-68,102,98,82,-27,63,25,8,-118,16,47,-125,96,-42,-65,26,27,9,-71,59,-91,-84,30,-122,-60,-65,17,-64,-85,45,115,25,-127,-30,63,25,108,-124,-89,-20,30,-103,-23,63,-86,1,87,10,27,9,-65,42,74,-75,-85,-122,-25,63,17,124,6,35,23,-110,33,-34,-65,25,121,49,50,9,95,54,-33,63,18,27,9,-2,86,-32,86,61,-93,-47,63,17,76,-18,70,-11,66,-56,-36,-65,25,-71,103,-118,-49,-45,47,-21,-65,26,27,9,52,-34,-56,19,104,-47,-29,63,17,57,-37,16,55,-47,73,-24,63,25,20,63,-118,-11,-105,-74,-55,-65,-86,1,87,10,27,9,-68,42,74,-75,-85,-122,-25,63,17,-124,6,35,23,-110,33,-34,-65,25,124,49,50,9,95,54,-33,63,18,27,9,106,-40,30,-105,122,-15,-55,-65,17,-113,79,-115,-38,-29,-38,-22,-65,25,-50,-20,60,14,-63,37,-32,-65,26,27,9,-114,-96,17,-63,1,-77,-28,63,17,-16,-81,92,44,-82,106,-47,63,25,-36,-16,-95,-59,11,-52,-26,-65,106,15,13,-67,16,83,64,21,-4,-66,-18,62,29,-98,-100,-105,67,-94,1,27,9,124,1,-63,-119,-20,93,72,-64,17,-15,-122,51,-96,93,-96,91,64,25,50,35,17,72,97,61,55,-64,-86,1,87,10,27,9,14,-87,56,-6,95,-3,-18,63,17,31,-40,-1,95,-36,-1,-73,63,25,-103,52,122,47,5,-111,-51,63,18,27,9,-42,14,36,48,-21,-20,-73,-65,17,71,-47,117,-41,-121,-37,-17,63,25,100,73,89,46,-59,5,-119,-65,26,27,9,-104,15,107,116,-37,-108,-51,-65,17,-47,-102,120,-108,-69,-6,-125,-65,25,-31,25,-38,-25,-44,33,-17,63,-79,1,-40,30,47,46,24,-6,-31,63,-6,1,15,13,0,0,0,0,21,-84,16,40,67,29,0,0,0,0,-126,2,15,13,36,-81,69,67,21,36,-81,69,67,29,103,81,-8,66,-107,2,-84,127,-26,66,-78,2,-77,1,8,-110,-124,-8,21,18,13,76,80,55,56,55,56,55,57,49,51,53,51,54,29,7,114,19,64,37,-52,116,0,64,45,0,0,-21,67,50,15,13,0,0,0,0,21,0,0,0,0,29,0,0,0,0,72,1,80,0,88,1,96,0,104,7,112,1,120,1,-128,1,1,-118,1,5,49,46,55,46,48,-107,1,-22,-87,-26,66,-102,1,87,10,27,9,0,0,0,0,0,0,-16,63,17,0,0,0,0,0,0,0,0,25,0,0,0,0,0,0,0,0,18,27,9,0,0,0,0,0,0,0,0,17,0,0,0,0,0,0,-16,63,25,0,0,0,0,0,0,0,0,26,27,9,0,0,0,0,0,0,0,0,17,0,0,0,0,0,0,0,0,25,0,0,0,0,0,0,-16,63,-91,1]";
    }

    @Test
    public void testSerialisation(){

        Controller ctrl = new Controller();


        Frame frame = Serializer.sequenceFromJson(serialized);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        String serializedHand = HandFactory.handToHandJoint("h1", original);

        String[] components = serializedHand.split(",");

        float[] joints = new float[components.length - 1];

        for (int i = 1; i < components.length; i++){
            joints[i-1] = Float.parseFloat(components[i]);
        }

        int counter = 0;

        for (Finger.Type ft : fingerTypes){
            for (Bone.Type bt : fingerBoneTypes){

                if (bt.equals(Bone.Type.TYPE_METACARPAL) && ft.equals(Finger.Type.TYPE_THUMB)){
                    continue;
                }

                String error = ft + ":" + bt + " is incorrectly deserialised.";

                Vector prevJ = original.fingers().fingerType(ft).get(0).bone(bt).prevJoint();
                Vector prevJNew = new Vector(joints[counter], joints[counter+1], joints[counter+2]);
                assertVectorEquals(error, prevJ, prevJNew);

                Vector nextJ = original.fingers().fingerType(ft).get(0).bone(bt).nextJoint();
                Vector nextJNew = new Vector(joints[counter+3], joints[counter+4], joints[counter+5]);
                assertVectorEquals(error, nextJ, nextJNew);

                counter += 6;

            }
            // finger tip position
            Vector origTip = original.fingers().fingerType(ft).get(0).tipPosition();
            Vector newTip = new Vector(joints[counter], joints[counter+1],
                    joints[counter+2]);
            assertVectorEquals("Tip position does not match.", origTip,
                    newTip);
            counter += 3;
        }
    }


    @Test
    public void testReconstruction(){


        Frame frame = Serializer.sequenceFromJson(serialized);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        String serializedHand = HandFactory.handToHandJoint("h1", original);

        Frame f = new Frame();

        Hand restored = HandFactory.createHand(serializedHand, f);

        for (Finger.Type ft : fingerTypes){
            assertFingerEquals(original.fingers().fingerType(ft).get(0),
                    restored.fingers().fingerType(ft).get(0));
        }

        assertFingerEquals(original.fingers().frontmost(),
                restored.fingers().frontmost());

    }


    @Test
    public void testPosition(){


        Frame frame = Serializer.sequenceFromJson(serialized);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        FrameDeconstructor fd = new FrameDeconstructor();

        String[] vect = fd.getHandPosition(original).trim().split(",");

        Vector pos = new Vector(Float.parseFloat(vect[1]), Float.parseFloat(vect[2]), Float.parseFloat(vect[3]));

        assertVectorEquals("Position is incorrect.", original.palmPosition(), pos);

    }


    public void assertVectorEquals(Vector v1, Vector v2){
        assertEquals(v1.getX(), v2.getX(), 0.000001f);
        assertEquals(v1.getY(), v2.getY(), 0.000001f);
        assertEquals(v1.getZ(), v2.getZ(), 0.000001f);
    }

    public void assertVectorEquals(String error, Vector v1, Vector v2){
        assertEquals(error, v1.getX(), v2.getX(), 0.000001f);
        assertEquals(error, v1.getY(), v2.getY(), 0.000001f);
        assertEquals(error, v1.getZ(), v2.getZ(), 0.000001f);
    }

    public void assertFingerEquals(Finger f1, Finger f2){
        for (Bone.Type bt : fingerBoneTypes){

            String error = f1.type() + ":" + bt + " is incorrectly reconstructed!";

            assertVectorEquals(error, f1.bone(bt).nextJoint(),
                    f2.bone(bt).nextJoint());

            assertVectorEquals(error, f1.bone(bt).prevJoint(),
                    f2.bone(bt).prevJoint());

            assertVectorEquals(error, f1.bone(bt).direction(),
                    f2.bone(bt).direction());


            assertVectorEquals(error, f1.bone(bt).center(),
                    f2.bone(bt).center());

        }

        Vector tip1 = f1.tipPosition();

        Vector lastJointPosDir = f1.bone(Bone.Type.TYPE_DISTAL).direction();

        Vector tip2 = f2.tipPosition();

        assertVectorEquals("Finger Tip Position reconstruction error: " +
                        tip1 + tip2 + lastJointPosDir + tip2.minus(tip1),
                tip1, tip2);
    }

    @Test
    public void testReconstructionSetBasis(){


        Frame frame = Serializer.sequenceFromJson(serialized);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        String serializedHand = HandFactory.handToHandJoint("h1", original);

        Frame f = new Frame();

        SeededHand restored = HandFactory.createHand(serializedHand, f);

        FrameDeconstructor fd = new FrameDeconstructor();

        String[] rotation = fd.getHandRotationModel(original).split(",");

        Quaternion rot = new Quaternion(Float.parseFloat(rotation[1]),
                Float.parseFloat(rotation[2]),
                Float.parseFloat(rotation[3]),
                Float.parseFloat(rotation[4]));

        String[] vect = fd.getHandPosition(original).trim().split(",");

        Vector pos = new Vector(Float.parseFloat(vect[1]), Float.parseFloat(vect[2]), Float.parseFloat(vect[3]));

        rot.setBasis(restored);

        restored.setOrigin(pos);


        for (Finger.Type ft : fingerTypes){
            assertFingerEquals(original.fingers().fingerType(ft).get(0),
                    restored.fingers().fingerType(ft).get(0));
        }

        assertFingerEquals(original.fingers().frontmost(),
                restored.fingers().frontmost());



    }

    @Test
    public void testReconstructionSetComplete(){


        Frame frame = Serializer.sequenceFromJson(serialized);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        String serializedHand = HandFactory.handToHandJoint("h1", original);

        Frame f = new Frame();

        SeededHand restored = HandFactory.createHand(serializedHand, f);

        FrameDeconstructor fd = new FrameDeconstructor();

        String[] rotation = fd.getHandRotationModel(original).split(",");

        Quaternion rot = new Quaternion(Float.parseFloat(rotation[1]),
                Float.parseFloat(rotation[2]),
                Float.parseFloat(rotation[3]),
                Float.parseFloat(rotation[4]));

        String[] vect = fd.getHandPosition(original).trim().split(",");

        Vector pos = new Vector(Float.parseFloat(vect[1]), Float.parseFloat(vect[2]), Float.parseFloat(vect[3]));

        restored.setRotation(rot);

        restored.setOrigin(pos);


        for (Finger.Type ft : fingerTypes){
            assertFingerEquals(original.fingers().fingerType(ft).get(0),
                    restored.fingers().fingerType(ft).get(0));
        }

        assertFingerEquals(original.fingers().frontmost(),
                restored.fingers().frontmost());



    }


    @Ignore
    public void testReconstructionStabilized(){


        Frame frame = Serializer.sequenceFromJson(serialized);
        Frame next = Serializer.sequenceFromJson(serializedNext);

        Hand original = frame.hand(0);

        if (!original.isValid()){
            for (Hand h : frame.hands()){
                if (h.isValid()){
                    original = h;
                }
            }
        }

        Hand originalNext = frame.hand(0);

        if (!originalNext.isValid()){
            for (Hand h : next.hands()){
                if (h.isValid()){
                    originalNext = h;
                }
            }
        }

        String serializedHand = HandFactory.handToHandJoint("h1", original);


        SeededHand restored = restore(original, serializedHand);

        SeededHand nextRestored = restore(originalNext, HandFactory.handToHandJoint("h2", originalNext));

        ArrayList<SeededHand> hands = new ArrayList<SeededHand>(2);
        hands.add(restored);
        hands.add(nextRestored);

        Hand h2 = restored.fadeHand(hands, 1.0f);

        Finger front1 = originalNext.fingers().frontmost();
        Finger front2 = h2.fingers().frontmost();

        Vector tip1 = front1.stabilizedTipPosition();
        Vector tip2 = front2.stabilizedTipPosition();

        assertVectorEquals("Stablized Tip Positions should be equivalent: \n" + tip1 + "\n" + tip2 + "\n(" +
                front1.tipVelocity() + " " + front1.tipPosition() + ") (" + front2.tipVelocity() + " " + front2.tipPosition() + ")",
                tip1,
                tip2);



    }

    public SeededHand restore(Hand original, String serializedHand){
        Frame f = new Frame();

        SeededHand restored = HandFactory.createHand(serializedHand, f);

        FrameDeconstructor fd = new FrameDeconstructor();

        String[] rotation = fd.getHandRotationModel(original).split(",");

        Quaternion rot = new Quaternion(Float.parseFloat(rotation[1]),
                Float.parseFloat(rotation[2]),
                Float.parseFloat(rotation[3]),
                Float.parseFloat(rotation[4]));

        String[] vect = fd.getHandPosition(original).trim().split(",");

        Vector pos = new Vector(Float.parseFloat(vect[1]), Float.parseFloat(vect[2]), Float.parseFloat(vect[3]));

        restored.setRotation(rot);

        restored.setOrigin(pos);

        return restored;
    }

}
