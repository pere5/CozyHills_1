package com.cozyhills.rules;

import com.cozyhills.cozy.StateHolder;
import com.cozyhills.cozy.Util;
import com.cozyhills.things.Person;
import com.cozyhills.things.VisibleEntity;
import com.cozyhills.things.buildings.Home;
import com.cozyhills.things.items.Item;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by pere5 on 02/01/16.
 */
public abstract class RuleHelper implements Rule {

    private final int rank;
    protected final int id;

    public RuleHelper(int rank) {
        this.rank = rank;
        this.id = Integer.MAX_VALUE - rank;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public void printInfo(int status) {
        Util.print(id + ":" + status);
    }

    protected static double range(VisibleEntity entity1, VisibleEntity entity2) {
        return Math.sqrt(Math.pow((entity1.xy[0] - entity2.xy[0]), 2) + Math.pow((entity1.xy[1] - entity2.xy[1]), 2));
    }

    protected static double rangeSimplified(VisibleEntity entity1, VisibleEntity entity2) {
        return Math.pow((entity1.xy[0] - entity2.xy[0]), 2) + Math.pow((entity1.xy[1] - entity2.xy[1]), 2);
    }

    protected static double[] centroid(Set<VisibleEntity> visibleEntityList) {
        double[] centroid = { 0, 0 };

        for (VisibleEntity visibleEntity: visibleEntityList) {
            centroid[0] += visibleEntity.xy[0];
            centroid[1] += visibleEntity.xy[1];
        }

        int totalPoints = visibleEntityList.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

    protected static double[] randomDestination(Person me, int distance) {
        double r1 = 1 - ThreadLocalRandom.current().nextInt(0, 2 + 1);
        double r2 = 1 - ThreadLocalRandom.current().nextInt(0, 2 + 1);
        return new double[]{me.xy[0] + distance * r1, me.xy[1] + distance * r2};
    }

    protected Optional getClosestVisibleResourceFromItemSet(Person me, int visibleZone, Set<Class> itemTypes) {
        return itemTypes.parallelStream()
                .map(this::getCorrespondingResourceFromItemType)
                .filter(Optional::isPresent)
                .map(type -> getClosestVisibleEntity(me, visibleZone, type.get()))
                .filter(Optional::isPresent)
                .min(Comparator.comparingDouble(optional -> rangeSimplified(me, optional.get()))).orElse(Optional.empty());
    }

    private Optional<Class> getCorrespondingResourceFromItemType(Class itemType) {
        try {
            return Optional.of(((Item)itemType.newInstance()).getCorrespondingResource());
        } catch (IllegalAccessException | InstantiationException e) {
            return Optional.empty();
        }
    }

    protected Optional getClosestVisibleEntityOfTypeSet(Person me, int visibleZone, Set<Class> typeSet) {
        return typeSet.parallelStream()
                .map(type -> getClosestVisibleEntity(me, visibleZone, type)) //all closest entities
                .filter(Optional::isPresent)
                .min(Comparator.comparingDouble(optional -> rangeSimplified(me, optional.get()))); //closest
    }

    protected Optional<Home> getClosestUnvisitedVisibleHome(Person me, int visibleZone) {
        return getHomes().parallelStream()
                .filter(me::notVisited) //Unvisited
                .min(Comparator.comparingDouble(home -> rangeSimplified(me, home))) //Closest
                .map(result -> range(me, result) <= visibleZone ? result : null); //Visible
    }

    protected Optional<VisibleEntity> getClosestVisibleEntity(Person me, int visibleZone, Class entityType) {
        return getEntityList(entityType).parallelStream()
                .min(Comparator.comparingDouble(entity -> rangeSimplified(me, (VisibleEntity) entity))) //Closest
                .map(result -> range(me, result) <= visibleZone ? result : null); //Visible
    }

    private Set<? extends VisibleEntity> getEntityList(Class entity) {
        return StateHolder.getEntities(entity);
    }

    protected Set<Person> getPersons() {
        return StateHolder.getPersons();
    }

    protected Set<Home> getHomes() {
        return StateHolder.getHomes();
    }
}
